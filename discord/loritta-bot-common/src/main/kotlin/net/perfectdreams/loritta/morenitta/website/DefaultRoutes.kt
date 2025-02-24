package net.perfectdreams.loritta.morenitta.website

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.AdsTxtRoute
import net.perfectdreams.loritta.morenitta.website.routes.CommunityGuidelinesRoute
import net.perfectdreams.loritta.morenitta.website.routes.DailyRoute
import net.perfectdreams.loritta.morenitta.website.routes.DonateRoute
import net.perfectdreams.loritta.morenitta.website.routes.FanArtsArtistRoute
import net.perfectdreams.loritta.morenitta.website.routes.FanArtsRoute
import net.perfectdreams.loritta.morenitta.website.routes.SponsorsRoute
import net.perfectdreams.loritta.morenitta.website.routes.TermsOfServiceRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.CreateWebhookRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.GetPubSubHubbubCallbackRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.PostDiscordBotsCallbackRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.PostPerfectPaymentsCallbackRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.PostPubSubHubbubCallbackRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetDailyShopRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetLoriDailyRewardRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetLoriDailyRewardStatusRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostBuyDailyShopItemRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostTransferBalanceExternalRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetGuildInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetGuildWebAuditLogRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetMembersWithPermissionsInGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetMembersWithRolesInGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetServerConfigSectionRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PatchServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostObsoleteServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostSearchGuildsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostSendMessageGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetAvailableBackgroundsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetAvailableProfileDesignsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetFanArtsController
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetLocaleRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetLorittaActionRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetPrometheusMetricsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetRaffleStatusRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.GetStatusRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostErrorRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostLorittaActionRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostRaffleStatusRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostReputationMessageRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostTransferBalanceRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostUpdateReadyRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.PostUpdateUserBackgroundRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitch.GetTwitchInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitter.GetShowTwitterUserRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetMutualGuildsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetSelfInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetSelfUserProfileRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetUserReputationsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PatchProfileRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostDeleteDataRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostDonationPaymentRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostLogoutRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostSearchUsersRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostUserReputationsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.youtube.GetChannelInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.DashboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.AuditLogRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureAutoroleRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureCustomBadgeRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureCustomCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureDailyMultiplierRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureEconomyRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureEventLogRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureGeneralRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureInviteBlockerRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureLevelUpRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureMemberCounterRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureMiscellaneousRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureModerationRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureNashornCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigurePermissionsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigurePremiumKeyRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureStarboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureTrackedTwitterAccountsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureTwitchRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureWelcomerRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureYouTubeRoute
import net.perfectdreams.loritta.morenitta.website.routes.sponsors.SponsorsRedirectRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.UserDashboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.UserReputationRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.AllBackgroundsListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.AvailableBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.BackgroundsListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.DailyShopRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ProfileListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ShipEffectsRoute

object DefaultRoutes {
	fun defaultRoutes(loritta: LorittaBot, website: LorittaWebsite) = listOf(
		// ===[ USER ROUTES ]===
		CommunityGuidelinesRoute(loritta),
		FanArtsArtistRoute(loritta),
		FanArtsRoute(loritta),
		SponsorsRoute(loritta),
		SponsorsRedirectRoute(loritta),
		TermsOfServiceRoute(loritta),
		DailyRoute(loritta),
		DonateRoute(loritta),

		// Dashboard
		DashboardRoute(loritta),
		ConfigureGeneralRoute(loritta),
		AuditLogRoute(loritta),
		ConfigureAutoroleRoute(loritta),
		ConfigureCommandsRoute(loritta),
		ConfigureCustomBadgeRoute(loritta),
		ConfigureDailyMultiplierRoute(loritta),
		ConfigureEconomyRoute(loritta),
		ConfigureEventLogRoute(loritta),
		ConfigureInviteBlockerRoute(loritta),
		ConfigureLevelUpRoute(loritta),
		ConfigureTwitchRoute(loritta),
		ConfigureMemberCounterRoute(loritta),
		ConfigureMiscellaneousRoute(loritta),
		ConfigureModerationRoute(loritta),
		ConfigurePermissionsRoute(loritta),
		ConfigurePremiumKeyRoute(loritta),
		ConfigureStarboardRoute(loritta),
		ConfigureTrackedTwitterAccountsRoute(loritta),
		ConfigureWelcomerRoute(loritta),
		ConfigureYouTubeRoute(loritta),
		ConfigureNashornCommandsRoute(loritta),
		ConfigureCustomCommandsRoute(loritta),

		// Reps
		UserReputationRoute(loritta),

		// Profiles
		UserDashboardRoute(loritta),
		ProfileListRoute(loritta),
		ShipEffectsRoute(loritta),
		AvailableBundlesRoute(loritta),
		BackgroundsListRoute(loritta),
		AllBackgroundsListRoute(loritta),
		DailyShopRoute(loritta),

		// ===[ API ROUTES ]===
		// Callbacks
		GetPubSubHubbubCallbackRoute(loritta),
		PostDiscordBotsCallbackRoute(loritta),
		PostPubSubHubbubCallbackRoute(loritta),
		PostPerfectPaymentsCallbackRoute(loritta),
		CreateWebhookRoute(loritta),

		// Economy
		GetLoriDailyRewardRoute(loritta),
		GetLoriDailyRewardStatusRoute(loritta),
		PostTransferBalanceExternalRoute(loritta),
		GetBundlesRoute(loritta),
		PostBundlesRoute(loritta),
		GetDailyShopRoute(loritta),
		PostBuyDailyShopItemRoute(loritta),

		// Guild
		GetGuildInfoRoute(loritta),
		GetGuildWebAuditLogRoute(loritta),
		GetMembersWithPermissionsInGuildRoute(loritta),
		GetMembersWithRolesInGuildRoute(loritta),
		GetServerConfigRoute(loritta, website),
		PatchServerConfigRoute(loritta, website),
		PostObsoleteServerConfigRoute(loritta),
		PostSearchGuildsRoute(loritta),
		PostSendMessageGuildRoute(loritta),
		GetServerConfigSectionRoute(loritta, website),

		// Loritta
		GetCommandsRoute(loritta),
		GetFanArtsController(loritta),
		GetLocaleRoute(loritta),
		GetLorittaActionRoute(loritta),
		GetRaffleStatusRoute(loritta),
		GetStatusRoute(loritta),
		GetPrometheusMetricsRoute(loritta),
		GetAvailableBackgroundsRoute(loritta),
		GetAvailableProfileDesignsRoute(loritta),
		GetSelfUserProfileRoute(loritta),
		PostLorittaActionRoute(loritta),
		PostRaffleStatusRoute(loritta),
		PostReputationMessageRoute(loritta),
		PostTransferBalanceRoute(loritta),
		PostUpdateReadyRoute(loritta),
		PostUpdateUserBackgroundRoute(loritta),
		PostErrorRoute(loritta),

		// Twitch
		GetTwitchInfoRoute(loritta),

		// Twitter
		GetShowTwitterUserRoute(loritta),

		// User
		GetMutualGuildsRoute(loritta),
		GetSelfInfoRoute(loritta),
		GetUserReputationsRoute(loritta),
		PatchProfileRoute(loritta),
		PostDonationPaymentRoute(loritta),
		PostSearchUsersRoute(loritta),
		PostUserReputationsRoute(loritta),
		PostLogoutRoute(loritta),
		PostDeleteDataRoute(loritta),

		// Twitch
		GetTwitchInfoRoute(loritta),

		// YouTube
		GetChannelInfoRoute(loritta),

		// ===[ MISC ]===
		AdsTxtRoute(loritta)
	)
}