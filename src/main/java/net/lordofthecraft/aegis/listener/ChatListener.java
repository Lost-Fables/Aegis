package net.lordofthecraft.aegis.listener;

import de.exceptionflug.protocolize.api.event.PacketReceiveEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.lordofthecraft.aegis.Aegis;
import net.md_5.bungee.protocol.packet.Chat;

public class ChatListener extends PacketAdapter<Chat> {

	Aegis plugin;

	public ChatListener(Aegis plugin) {
		super(Stream.UPSTREAM, Chat.class);
		this.plugin = plugin;
	}

	@Override
	public void receive(PacketReceiveEvent<Chat> event) {
		if (plugin.getDaemon().isAwaitingAuthentication(event.getPlayer().getUniqueId())) {
			if (!event.getPacket().getMessage().split(" ")[0].equalsIgnoreCase("/auth")) {
				event.setCancelled(true);
			}
		}
	}

}
