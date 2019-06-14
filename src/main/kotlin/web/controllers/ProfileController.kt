package web.controllers

import domain.ProfileDTO
import domain.service.UserService
import io.javalin.Context

class ProfileController(private val userService: UserService) {
    fun get(ctx: Context) {
        ctx.validatedPathParam("username").getOrThrow().also { usernameFollowing ->
            userService.getProfileByUsername(ctx.attribute("email")!!, usernameFollowing).also { profile ->
                ctx.json(ProfileDTO(profile))
            }
        }
    }

    fun follow(ctx: Context) {
        ctx.validatedPathParam("username").getOrThrow().also { usernameToFollow ->
            userService.follow(ctx.attribute("email")!!, usernameToFollow).also { profile ->
                ctx.json(ProfileDTO(profile))
            }
        }
    }

    fun unfollow(ctx: Context) {
        ctx.validatedPathParam("username").getOrThrow().also { usernameToUnfollow ->
            userService.unfollow(ctx.attribute("email")!!, usernameToUnfollow).also { profile ->
                ctx.json(ProfileDTO(profile))
            }
        }
    }
}
