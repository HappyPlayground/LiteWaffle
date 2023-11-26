package com.discord.sapokr.litewaffle

import club.minnced.discord.webhook.WebhookClient
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.Channels
import java.util.regex.Pattern

class LiteWaffle : JavaPlugin() {
    companion object {
        var Jda: JDA? = null

        var isDebug: Boolean = false

        var ActiveListeners: MutableMap<String, LWSetting> = mutableMapOf()

        class LWSetting(initname: String) {
            var displayname: String = initname
            var listeners: ArrayList<ListenerAdapter> = ArrayList()
        }

        fun LWInitialize(initname: String): LWUtility {
            if (ActiveListeners.keys.contains(initname)) {
               return LWUtility(initname+ ActiveListeners.keys.count {it == initname})
            }

            ActiveListeners[initname] =  LWSetting(initname)

            return LWUtility(initname)
        }

        fun getStatus(): BotStatus {
            val listeners = (Jda!!.registeredListeners + MsgListener).map {
                it.javaClass.simpleName + if (it in MsgListener) "(M)" else ""
            }

            val webhooks = cfg!!.getConfigurationSection("webhooks")!!.getKeys(false).associateWith { key ->
                val request = HttpRequest.newBuilder().uri(URI.create(cfg!!.getString("webhooks.$key")!!)).build()
                val response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.discarding())
                response.statusCode() != 404
            }

            return BotStatus(
                Litewaffle = LitewaffleStatus(
                    isOnline = if (Jda!!.status.isInit) "Online" else "Offline",
                    account = Jda!!.selfUser.name,
                    listeners = listeners
                ),
                webhooks = webhooks.toMutableMap()
            )
        }


        class LWUtility(var initname: String) {
            fun setDisplayname(displayname: String) {
                ActiveListeners[initname]!!.displayname = displayname
            }

            fun addListenerHandler(handler: ListenerAdapter){
                if(ActiveListeners[initname]!!.listeners.contains(handler)) {
                    return
                }

                ActiveListeners[initname]!!.listeners.add(handler)

                if (handler.javaClass.methods[0].name == "onMessageReceived") {
                    MsgListener.add(handler)
                } else {
                    Jda!!.addEventListener(handler)
                }

                return
            }

            fun removeListenerHandler(handler: String) {
                val msghandlers: ArrayList<String> = ArrayList()

                MsgListener.forEach {
                    msghandlers.add(it.javaClass.name)
                }

                if (msghandlers.contains(handler)) {
                    MsgListener.removeAt(msghandlers.indexOf(handler))
                } else {
                    Jda!!.removeEventListener(handler)
                }
            }
        }

        lateinit var Ops: Array<String>

        var webhooks: MutableMap<String, WebhookClient> = mutableMapOf()

        var cfg: FileConfiguration? = null

        var MsgListener:ArrayList<ListenerAdapter> = arrayListOf<ListenerAdapter>()

        var Prefix: String = "/"

        var ServerVersion: String = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)").matcher(Bukkit.getServer().version).takeIf { it.find() }!!.group(1)
    }

    fun downloadlanguageJson(plugin: Plugin, version: String, language: String = "ko_kr") {
        try {
            val assetsFolder = File(plugin.dataFolder, "assets")
            val jsonFile = File(assetsFolder, "$language.json")

            // JSON 파일이 존재하지 않으면 다운로드합니다.
            if (!jsonFile.exists()) {
                plugin.logger.info("$language.json does not exist. Downloading...")
                jsonFile.parentFile.mkdirs()
                jsonFile.createNewFile()

                val url = URL("https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/$version/assets/minecraft/lang/$language.json")
                url.openStream().use { inputStream ->
                    Channels.newChannel(inputStream).use { readableByteChannel ->
                        FileOutputStream(jsonFile).use { outputStream ->
                            outputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
                        }
                    }
                }

                plugin.logger.info("$language.json successfully downloaded.")
            }
        } catch (e: IOException) {
            plugin.logger.warning("Error downloading $language.json : " + e.message)
        }
    }
    override fun onEnable() {
        logger.info("ZeroSugar Waffle Active")

        saveDefaultConfig()

        cfg = config

        Ops = config.getStringList("options.owners").toTypedArray()

        Prefix = config.getString("prefix")!!

        for (key in cfg!!.getConfigurationSection("webhooks")!!.getKeys(false)) {
            webhooks[key] = WebhookClient.withUrl(config.getString("webhooks.$key")!!)
        }

        Jda = JDABuilder
            .createDefault(config.getString("token"))
            .build()

        Jda!!.addEventListener(JDAListener())

        Bukkit.getPluginManager().registerEvents(ToDiscord(), this)

        if (config.getBoolean("options.sendAdvancement")) {
            val language = config.getString("options.language")!!
            downloadlanguageJson(this, ServerVersion, language)
            Bukkit.getPluginManager().registerEvents(AdvancementListener(this, language), this)
        }

        getCommand("litewaffle")?.setExecutor(SupportCommands())
        getCommand("litewaffle")?.tabCompleter = CommandATComplete()
    }

    override fun onDisable() {
        logger.info("ZeroSugar Waffle(Dulcin[C9H12N2O2])")
        Jda?.shutdown()
    }
}