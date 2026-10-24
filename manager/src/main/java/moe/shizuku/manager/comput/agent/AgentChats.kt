package moe.shizuku.manager.comput.agent

import moe.shizuku.manager.module.ModuleSettings
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** One message of a saved agent chat. [role] is "user" or "assistant". */
data class AgentChatMessage(
    val role: String,
    val content: String,
)

/** A saved agent conversation: the first goal, its answer and every follow-up. */
data class AgentChat(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<AgentChatMessage>,
)

/**
 * Chat history of the Comput agent.
 *
 * Stored as one JSON string in the same SharedPreferences file the Comput
 * macros use (see [ModuleSettings]), so no database is needed: a bounded list
 * of chats, newest first, capped by [MAX_CHATS] and [MAX_MESSAGES].
 */
object AgentChats {

    const val MAX_CHATS = 30
    private const val MAX_MESSAGES = 200
    private const val MAX_CONTENT = 8000

    fun load(): List<AgentChat> {
        return try {
            val root = JSONArray(ModuleSettings.getAgentChats())
            val chats = mutableListOf<AgentChat>()
            for (i in 0 until root.length()) {
                val obj = root.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                if (id.isBlank()) continue
                val messagesArr = obj.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<AgentChatMessage>()
                for (j in 0 until messagesArr.length()) {
                    val m = messagesArr.optJSONObject(j) ?: continue
                    val content = m.optString("content")
                    if (content.isBlank()) continue
                    messages.add(AgentChatMessage(m.optString("role"), content))
                }
                if (messages.isEmpty()) continue
                chats.add(
                    AgentChat(
                        id = id,
                        title = obj.optString("title").ifBlank { messages.first().content.take(60) },
                        createdAt = obj.optLong("createdAt"),
                        updatedAt = obj.optLong("updatedAt"),
                        messages = messages,
                    )
                )
            }
            chats.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(chats: List<AgentChat>) {
        val root = JSONArray()
        chats
            .sortedByDescending { it.updatedAt }
            .take(MAX_CHATS)
            .forEach { chat ->
                val messages = JSONArray()
                chat.messages.takeLast(MAX_MESSAGES).forEach { message ->
                    messages.put(
                        JSONObject()
                            .put("role", message.role)
                            .put("content", message.content.take(MAX_CONTENT))
                    )
                }
                root.put(
                    JSONObject()
                        .put("id", chat.id)
                        .put("title", chat.title)
                        .put("createdAt", chat.createdAt)
                        .put("updatedAt", chat.updatedAt)
                        .put("messages", messages)
                )
            }
        ModuleSettings.setAgentChats(root.toString())
    }

    fun newChatId(): String = UUID.randomUUID().toString()
}
