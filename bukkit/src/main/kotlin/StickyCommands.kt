/*
 * Copyright (c) 2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information.
 */
package com.dumbdogdiner.stickycommands

import com.dumbdogdiner.stickyapi.bukkit.util.StartupUtil
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider
import com.dumbdogdiner.stickycommands.api.StickyCommands
import com.dumbdogdiner.stickycommands.api.economy.Market
import com.dumbdogdiner.stickycommands.api.managers.PowertoolManager
import com.dumbdogdiner.stickycommands.database.PostgresHandler
import com.dumbdogdiner.stickycommands.economy.StickyMarket
import com.dumbdogdiner.stickycommands.managers.StickyPlayerStateManager
import com.dumbdogdiner.stickycommands.managers.StickyPowertoolManager
import com.dumbdogdiner.stickycommands.timers.AfkTimer
import com.dumbdogdiner.stickycommands.util.WorthTable
import com.dumbdogdiner.stickycommands.util.sticky.StickyStartupUtil
import dev.jorel.commandapi.CommandAPI
import kr.entree.spigradle.annotations.PluginMain
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

@PluginMain
class StickyCommands : JavaPlugin(), StickyCommands {
    companion object {
        lateinit var plugin: com.dumbdogdiner.stickycommands.StickyCommands
        var economy: Economy? = null
        var localeProvider: LocaleProvider? = null
        var staffFacilitiesEnabled = false
    }

    lateinit var postgresHandler: PostgresHandler
    lateinit var worthTable: WorthTable
    lateinit var afkTimer: AfkTimer

    private val stickyPlayerStateManager = StickyPlayerStateManager()
    private val stickyPowertoolManager = StickyPowertoolManager()
    private val stickyMarket = StickyMarket()

    override fun onLoad() {
        plugin = this
        CommandAPI.onLoad(true)
        postgresHandler = PostgresHandler()

        worthTable = WorthTable()
        afkTimer = AfkTimer()

        if (!StickyStartupUtil.setupConfig()) return

        localeProvider = StartupUtil.setupLocale(this, localeProvider)
        if (localeProvider == null) return

        if (!postgresHandler.init()) return
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)
        StickyCommands.registerService(this, this)

        if (!StickyStartupUtil.setupPlaceholders())
                logger.severe("PlaceholderAPI is not available, is it installed?")

        if (!StickyStartupUtil.setupEconomy())
                logger.severe("Disabled economy commands due to no Vault dependency found!")

        if (!StickyStartupUtil.setupStaffFacilities())
                logger.severe("StaffFacilities not found, disabling integration")

        StickyStartupUtil.registerCommands()
        StickyStartupUtil.registerListeners()
        StickyStartupUtil.registerTimers(afkTimer)
    }

    override fun onDisable() {
        reloadConfig()
        afkTimer.cancel()
    }

    override fun getProvider(): Plugin {
        return this
    }

    override fun getPlayerStateManager(): StickyPlayerStateManager {
        return stickyPlayerStateManager
    }
    override fun getPowertoolManager(): PowertoolManager {
        return stickyPowertoolManager
    }

    override fun getMarket(): Market {
        return stickyMarket
    }
}
