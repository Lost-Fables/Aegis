package net.lordofthecraft.aegis.listener;

import co.lotc.core.bungee.util.ChatBuilder;
import de.exceptionflug.protocolize.api.event.PacketReceiveEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.lordofthecraft.aegis.Aegis;
import net.md_5.bungee.protocol.packet.Chat;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.RED;

public class ChatReceiveListener extends PacketAdapter<Chat> {

	private Aegis plugin;

	public ChatReceiveListener(Aegis plugin) {
		super(Stream.DOWNSTREAM, Chat.class);
		this.plugin = plugin;
	}

	@Override
	public void receive(PacketReceiveEvent<Chat> event) {
		if (plugin.getDaemon().isAwaitingAuthentication(event.getPlayer().getUniqueId())) {
			if (!event.getPacket().getMessage().split(" ")[0].equalsIgnoreCase("/auth")) {
				new ChatBuilder("Use ").color(RED).append("/auth").color(GOLD).append(" to authenticate").color(RED);
				event.setCancelled(true);
			}
		}
	}

}
