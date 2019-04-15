package net.lordofthecraft.aegis.listener;

import de.exceptionflug.protocolize.api.event.PacketSendEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AuthenticationDaemon;
import net.md_5.bungee.protocol.packet.Chat;

public class ChatSendListener extends PacketAdapter<Chat> {

	Aegis plugin;

	public ChatSendListener(Aegis plugin) {
		super(Stream.DOWNSTREAM, Chat.class);
		this.plugin = plugin;
	}

	@Override
	public void send(PacketSendEvent<Chat> event) {
		if (plugin.getDaemon().isAwaitingAuthentication(event.getPlayer().getUniqueId())) {
			if (!event.getPacket().getMessage().startsWith("SUCCESSFULLY") || event.getPacket().getMessage().contains(AuthenticationDaemon.BACKUP_CODES)) {
				plugin.getDaemon().queueMessage(event.getPlayer(), event.getPacket());
				event.setCancelled(true);
			}
		}
	}

}
