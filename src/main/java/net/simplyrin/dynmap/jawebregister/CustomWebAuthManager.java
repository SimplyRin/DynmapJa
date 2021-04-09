package net.simplyrin.dynmap.jawebregister;

import java.util.HashMap;
import java.util.Random;

import org.dynmap.DynmapCore;
import org.dynmap.WebAuthManager;
import org.dynmap.common.DynmapCommandSender;
import org.dynmap.common.DynmapPlayer;
import org.joor.Reflect;

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
public class CustomWebAuthManager extends WebAuthManager {

	private Main instance;

	public CustomWebAuthManager(DynmapCore core, Main instance) {
		super(core);
		this.instance = instance;
	}

	private Random rnd = new Random();

	@Override
	public boolean processWebRegisterCommand(DynmapCore core, DynmapCommandSender sender, DynmapPlayer player, String[] args) {
		HashMap<String, String> pending_registrations = Reflect.on(this).field("pending_registrations").get();

		String uid = null;
		boolean other = false;
		if (args.length > 1) {
			if (!core.checkPlayerPermission(sender, "webregister.other")) {
				sender.sendMessage(this.instance.getMessage("No-Permission"));
				return true;
			}
			uid = args[1];
			other = true;
		} else if (player == null) {
			sender.sendMessage(this.instance.getMessage("Must-provide"));
			return true;
		} else {
			uid = player.getName();
		}
		if (checkUserName(uid) == false) {
			sender.sendMessage(this.instance.getMessage("Invalid"));
			return true;
		}
		String regkey = String.format("%04d-%04d", rnd.nextInt(10000), rnd.nextInt(10000));
		pending_registrations.put(uid.toLowerCase(), regkey.toLowerCase());

		for (String line : this.instance.getMessages("Registration")) {
			sender.sendMessage(line.replace("%uid", uid).replace("%code", regkey));
		}

		if (other) {
			DynmapPlayer p = core.getServer().getPlayer(uid);
			if (p != null) {
				for (String line : this.instance.getMessages("Registration")) {
					sender.sendMessage(line.replace("%uid", uid).replace("%code", regkey));
				}
			}
		}
		core.events.trigger("loginupdated", null);
		return true;
	}

}
