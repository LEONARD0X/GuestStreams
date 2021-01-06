package de.leonardox.gueststreams;

import java.util.function.Consumer;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;

public class GuestStreams implements Consumer<DiscordApi> {

	// Your token here
	public static final String TOKEN = "";

	@Override
	public void accept(DiscordApi api) {
		api.addServerVoiceChannelMemberJoinListener(event -> {
			ServerVoiceChannel channel = event.getChannel();

			// Check if bot should operate in this channel
			if (!channel.getOverwrittenPermissions().containsKey(api.getClientId())) {
				return;
			}

			PermissionsBuilder builder = null;
			if (channel.getOverwrittenPermissions().containsKey(event.getUser().getId())) {
				builder = new PermissionsBuilder(channel.getOverwrittenPermissions().get(event.getUser().getId()));
			} else {
				builder = new PermissionsBuilder();
			}

			// Indicates that the member already has the permission to view this channel
			if (builder.getState(PermissionType.CONNECT) == PermissionState.ALLOWED) {
				return;
			}

			// Add temporary permissions to view and start steams
			builder.setState(PermissionType.STREAM, PermissionState.ALLOWED);
			builder.setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED);
			new ServerChannelUpdater(channel).addPermissionOverwrite(event.getUser(), builder.build()).update();
		});

		api.addServerVoiceChannelMemberLeaveListener(event -> {
			ServerVoiceChannel channel = event.getChannel();

			// Check if bot should operate in this channel
			if (!channel.getOverwrittenPermissions().containsKey(api.getClientId())) {
				return;
			}

			PermissionsBuilder builder = null;
			if (channel.getOverwrittenPermissions().containsKey(event.getUser().getId())) {
				builder = new PermissionsBuilder(channel.getOverwrittenPermissions().get(event.getUser().getId()));
			} else {
				builder = new PermissionsBuilder();
			}

			// Indicates that the member does not have temporary permissions
			if (builder.getState(PermissionType.CONNECT) == PermissionState.ALLOWED) {
				return;
			}

			// Remove temporary permissions
			new ServerChannelUpdater(channel).removePermissionOverwrite(event.getUser()).update();
		});
	}

	public static void main(String[] args) {
		new DiscordApiBuilder().setToken(TOKEN).login().thenAccept(new GuestStreams());
	}

}