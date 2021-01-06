package de.leonardox.gueststreams;

import java.util.function.Consumer;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;

public class GuestStreams implements Consumer<DiscordApi> {

	public static final String TOKEN = "";

	public static void main(String[] args) {
		new DiscordApiBuilder().setToken(TOKEN).login().thenAccept(new GuestStreams());
	}

	@Override
	public void accept(DiscordApi api) {
		api.addServerVoiceChannelMemberJoinListener(event -> {
			ServerVoiceChannel ch = event.getChannel().asServerVoiceChannel().get();
			if (!ch.getOverwrittenPermissions().containsKey(api.getClientId())) {
				return;
			}
			PermissionsBuilder builder = null;
			if (ch.getOverwrittenPermissions().containsKey(event.getUser().getId())) {
				builder = new PermissionsBuilder(ch.getOverwrittenPermissions().get(event.getUser().getId()));
			} else {
				builder = new PermissionsBuilder();
			}
			if (builder.getState(PermissionType.CONNECT) == PermissionState.ALLOWED) {
				return;
			}
			builder.setState(PermissionType.STREAM, PermissionState.ALLOWED);
			builder.setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED);
			Permissions perm = builder.build();
			new ServerChannelUpdater(ch).addPermissionOverwrite(event.getUser(), perm).update();
		});

		api.addServerVoiceChannelMemberLeaveListener(event -> {
			ServerVoiceChannel ch = event.getChannel().asServerVoiceChannel().get();
			if (!ch.getOverwrittenPermissions().containsKey(api.getClientId())) {
				return;
			}
			PermissionsBuilder builder = null;
			if (ch.getOverwrittenPermissions().containsKey(event.getUser().getId())) {
				builder = new PermissionsBuilder(ch.getOverwrittenPermissions().get(event.getUser().getId()));
			} else {
				builder = new PermissionsBuilder();
			}
			if (builder.getState(PermissionType.CONNECT) == PermissionState.ALLOWED) {
				return;
			}
			new ServerChannelUpdater(ch).removePermissionOverwrite(event.getUser()).update();
		});
	}
}
