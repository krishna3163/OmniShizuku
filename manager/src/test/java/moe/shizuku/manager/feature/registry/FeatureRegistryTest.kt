package moe.shizuku.manager.feature.registry

import moe.shizuku.manager.ui.Detail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureRegistryTest {

    @Test
    fun everyRegisteredFeatureHasAUniqueStableId() {
        val definitions = FeatureRegistry.all
        assertEquals(definitions.size, definitions.map { it.id }.toSet().size)
        assertTrue(definitions.all { it.id == it.id.lowercase() })
    }

    @Test
    fun everyLabsDetailHasRegistryMetadata() {
        val missing = Detail.entries.filter { FeatureRegistry.get(it) == null }
        assertEquals(emptyList<Detail>(), missing)
    }

    @Test
    fun privilegedFeaturesRequireAProvider() {
        val definition = FeatureRegistry.require(Detail.PRIVATE_DNS)
        assertTrue(definition.requiredProviders.contains(FeatureProvider.SHIZUKU))
        assertTrue(definition.requiredProviders.contains(FeatureProvider.ROOT))
    }
}
