package net.lordofthecraft.aegis.listener;

import de.exceptionflug.protocolize.api.event.PacketSendEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.lordofthecraft.aegis.Aegis;
import net.md_5.bungee.protocol.packet.Chat;

public class ChatReceiveListener extends PacketAdapter<Chat> {

	private Aegis plugin;

	public ChatReceiveListener(Aegis plugin) {
		super(Stream.UPSTREAM, Chat.class);
		this.plugin = plugin;
	}

	@Override
	public void send(PacketSendEvent<Chat> event) {
		if (plugin.getDaemon().isAwaitingAuthentication(event.getPlayer().getUniqueId())) {
			if (!event.getPacket().getMessage().split(" ")[0].equalsIgnoreCase("/auth")) {
				event.setCancelled(true);
			}
		}
	}

}
