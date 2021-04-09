package net.simplyrin.dynmap.jawebregister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCore;
import org.dynmap.WebAuthManager;
import org.dynmap.bukkit.DynmapPlugin;
import org.joor.Reflect;

import net.md_5.bungee.api.ChatColor;
import net.simplyrin.dynmap.jawebregister.commands.WebRegisterCommand;

/**
 * Created by SimplyRin on 2021/04/09.
 *
 * Copyright (c) 2021 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Main extends JavaPlugin {

	private WebAuthManager webAuthManager;
	private YamlConfiguration messages;

	@Override
	public void onEnable() {
		DynmapPlugin dynmap = DynmapPlugin.plugin;
		File configFile = new File(dynmap.getDataFolder(), "configuration.txt");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

		if (config.getBoolean("login-enabled")) {
			DynmapCore core = Reflect.on(dynmap).field("core").get();
			WebAuthManager authmgr = Reflect.on(core).field("authmgr").get();

			this.webAuthManager = authmgr;

			Reflect.on(core).set("authmgr", new CustomWebAuthManager(core, this));
			this.log("&aWebAuthManager クラスを変更しました。");

			this.getCommand("webregister").setExecutor(new WebRegisterCommand(this));
			this.reloadMessageFile();

			this.log("&a読み込みました。");
		} else {
			this.log("&cこのプラグインを使用するには、`dynmap/configuration.txt` を開き `login-enabled` を `true` に変更する必要があります。");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		if (this.webAuthManager != null) {
			DynmapPlugin dynmap = DynmapPlugin.plugin;
			DynmapCore core = Reflect.on(dynmap).field("core").get();
			Reflect.on(core).set("authmgr", this.webAuthManager);
		}
	}

	public void reloadMessageFile() {
		File messages = new File(this.getDataFolder(), "messages.yml");
		if (!messages.exists()) {
			this.saveResource("messages.yml", false);
		}

		this.messages = YamlConfiguration.loadConfiguration(messages);
	}

	public void log(String message) {
		this.getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] "
				+ ChatColor.translateAlternateColorCodes('&', message));
	}

	public String getMessage(String key) {
		return ChatColor.translateAlternateColorCodes('&', this.messages.getString(key, "Unknown key: " + key));
	}

	public List<String> getMessages(String key) {
		List<String> value = new ArrayList<>();
		for (String line : this.messages.getStringList(key)) {
			value.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		return value;
	}

}
