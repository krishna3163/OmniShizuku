import type { Metadata, Viewport } from "next"
import { Plus_Jakarta_Sans, JetBrains_Mono } from "next/font/google"
import "./globals.css"

const jakarta = Plus_Jakarta_Sans({
  subsets: ["latin", "cyrillic-ext"],
  weight: ["400", "500", "600", "700", "800"],
  variable: "--font-jakarta",
})

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin", "cyrillic"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-jetbrains",
})

export const metadata: Metadata = {
  title: "Shevery — Shizuku fork with Comput Agent, Device Owner & Stealth APK",
  description:
    "Shizuku fork targeting Android 17 (SDK 37) with a Material 3 Expressive Compose manager, 10-step Comput AI shell & module planner, Device Owner & Dhizuku v1/v2 provider, on-device Stealth APK byte-swap generator, and ADB modules.",
}

export const viewport: Viewport = {
  themeColor: "#070916",
  width: "device-width",
  initialScale: 1,
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" className={`${jakarta.variable} ${jetbrainsMono.variable} bg-background`}>
      <body className="antialiased">{children}</body>
    </html>
  )
}
