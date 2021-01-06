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

	public static final String TOKEN = ""; // Your token here

	@Override
	public void accept(DiscordApi api) {
		api.addServerVoiceChannelMemberJoinListener(event -> {
			ServerVoiceChannel channel = event.getChannel();

			// Check if bot should operate in this channel
			if (!channel.getOverwrittenPermissions().containsKey(api.getClientId())) {
				return;
			}

			// Check if member already has the permission to view this channel
			if (channel.getEffectivePermissions(event.getUser())
					.getState(PermissionType.READ_MESSAGES) == PermissionState.ALLOWED) {
				return;
			}

			// Add temporary permissions to view and start steams
			PermissionsBuilder builder = new PermissionsBuilder();
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

			// Check if member does not have temporary permissions
			if (!channel.getOverwrittenPermissions().containsKey(event.getUser().getId())) {
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