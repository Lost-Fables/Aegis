package net.lordofthecraft.aegis.listener;

import de.exceptionflug.protocolize.api.event.PacketSendEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AuthenticationDaemon;
import net.md_5.bungee.protocol.packet.Chat;

public class ChatSendListener extends PacketAdapter<Chat> {

	private Aegis plugin;

	public ChatSendListener(Aegis plugin) {
		super(Stream.UPSTREAM, Chat.class);
		this.plugin = plugin;
	}

	@Override
	public void send(PacketSendEvent<Chat> event) {
		if (plugin.getDaemon().isAwaitingAuthentication(event.getPlayer())) {
			String message = event.getPacket().getMessage();
			if (message.contains("SUCCESSFULLY") || message.contains(AuthenticationDaemon.BACKUP_CODES) || message.contains("Error: ")) {
				return;
			}

			plugin.getDaemon().queueMessage(event.getPlayer(), event.getPacket());
			event.setCancelled(true);
		}
	}

}
