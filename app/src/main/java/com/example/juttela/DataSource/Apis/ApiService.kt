package com.example.juttela.DataSource.Apis

import com.example.juttela.DataSource.Models.AcceptUsersModel
import com.example.juttela.DataSource.Models.ConnectionsRequest
import com.example.juttela.DataSource.Models.ConnectionsResponse
import com.example.juttela.DataSource.Models.GeoAddRequestModel
import com.example.juttela.DataSource.Models.GeoAddResponseModel
import com.example.juttela.DataSource.Models.GetIdRequest
import com.example.juttela.DataSource.Models.GetMessagesRequest
import com.example.juttela.DataSource.Models.GetMessagesResponse
import com.example.juttela.DataSource.Models.GetProfileRequest
import com.example.juttela.DataSource.Models.GetProfileResponse
import com.example.juttela.DataSource.Models.GetRequestsResponse
import com.example.juttela.DataSource.Models.MobileCheckResponse
import com.example.juttela.DataSource.Models.RequestAcceptedResponse
import com.example.juttela.DataSource.Models.RequestModels
import com.example.juttela.DataSource.Models.RequestResponse
import com.example.juttela.DataSource.Models.SendMessageRequest
import com.example.juttela.DataSource.Models.SendMessageResponse
import com.example.juttela.DataSource.Models.SendingSmartRequest
import com.example.juttela.DataSource.Models.SendingSmartRequestResponse
import com.example.juttela.DataSource.Models.SignupRequest
import com.example.juttela.DataSource.Models.SignupResponse
import com.example.juttela.DataSource.Models.SmartGetRequestBody
import com.example.juttela.DataSource.Models.SmartGetRequestResponse
import com.example.juttela.DataSource.Models.SmartMatchRequest
import com.example.juttela.DataSource.Models.SmartMatchResponse
import com.example.juttela.DataSource.Models.UpdateProfileRequest
import com.example.juttela.DataSource.Models.UpdateProfileResponse
import com.example.juttela.DataSource.Models.UserIdRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): SignupResponse

    @POST("api/users/check-mobile")
    suspend fun GetUserById(
        @Body request: UserIdRequest
    ): MobileCheckResponse

    @POST("api/users/geoAddUsers")
    suspend fun GeoAddUsers(
        @Body request : GeoAddRequestModel
    ): GeoAddResponseModel


    @POST("api/users/send-request")
    suspend fun SendRequest(
        @Body request : RequestModels
    ): RequestResponse


    @POST("api/users/get-requests")
    suspend fun GetRequestData(
        @Body request : GetIdRequest
    ): GetRequestsResponse


    @POST("api/users/accept-request")
    suspend fun AcceptRequest(
        @Body request: AcceptUsersModel
    ): RequestAcceptedResponse

    @POST("api/users/get-connections")
    suspend fun GetConnections(
        @Body request : ConnectionsRequest
    ) : ConnectionsResponse

    @POST("api/users/send-message")
    suspend fun SendMessageAPI(
        @Body request : SendMessageRequest
    ) : SendMessageResponse


    @POST("api/users/get-conversation")
    suspend fun GetMessages(
        @Body request : GetMessagesRequest
    ) : GetMessagesResponse

    @POST("api/users/user-update")
    suspend fun ProfileUpdate(
        @Body request : UpdateProfileRequest
    ) : UpdateProfileResponse

    @POST("api/users/get-profile")
    suspend fun GetProfile(
        @Body request : GetProfileRequest
    ) : GetProfileResponse


    @POST("api/users/smart-matching")
    suspend fun SmartMatching(
        @Body request : SmartMatchRequest
    ) : SmartMatchResponse


    @POST("api/users/Smart-get-request")
    suspend fun GetSmartMatching(
        @Body request : SmartGetRequestBody
    ) : SmartGetRequestResponse

    @POST("api/users/Smart-Send-request")
    suspend fun SmartSendRequest(
        @Body request : SendingSmartRequest
    ) : SendingSmartRequestResponse

}