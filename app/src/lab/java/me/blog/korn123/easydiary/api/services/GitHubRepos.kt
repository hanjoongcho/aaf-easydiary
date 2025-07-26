package me.blog.korn123.easydiary.api.services

import me.blog.korn123.easydiary.api.models.CommitRequest
import me.blog.korn123.easydiary.api.models.CommitResponse
import me.blog.korn123.easydiary.api.models.Contents
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
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

    @GET("/repos/{owner}/{repo}/contents/{path}")
    fun getContentsDetail(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String
    ): Call<Contents>

    @GET
    fun downloadContents(
        @Header("Authorization") token: String,
        @Url url: String
    ): Call<String>

    @PUT("/repos/{owner}/{repo}/contents/{path}")
    fun pushFile(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body body: CommitRequest
    ): Call<CommitResponse>
}