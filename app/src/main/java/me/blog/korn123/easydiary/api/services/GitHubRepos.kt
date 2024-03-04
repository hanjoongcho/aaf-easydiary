package me.blog.korn123.easydiary.api.services

import me.blog.korn123.easydiary.api.models.Contents
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface GitHubRepos {
    @GET("/repos/{owner}/{repo}/contents/{path}")
    fun findContents(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String
    ): Call<List<Contents>>

    @GET
    fun downloadContents(
        @Header("Authorization") token: String,
        @Url url: String
    ): Call<String>
}