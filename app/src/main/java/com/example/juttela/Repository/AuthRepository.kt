package com.example.juttela.Repository

import com.example.juttela.DataSource.Models.FeedbackRequest
import com.example.juttela.DataSource.Models.FeedbackResponse
import com.example.juttela.DataSource.Apis.RetrofitClient
import com.example.juttela.DataSource.Models.AcceptUsersModel
import com.example.juttela.DataSource.Models.AddUserToSessionRequest
import com.example.juttela.DataSource.Models.AddUserToSessionResponse
import com.example.juttela.DataSource.Models.ArrivalsResponse
import com.example.juttela.DataSource.Models.CancelSessionResponse
import com.example.juttela.DataSource.Models.ConnectionsRequest
import com.example.juttela.DataSource.Models.ConnectionsResponse
import com.example.juttela.DataSource.Models.DeleteAccountResponse
import com.example.juttela.DataSource.Models.GeoAddRequestModel
import com.example.juttela.DataSource.Models.GeoAddResponseModel
import com.example.juttela.DataSource.Models.GetIdRequest
import com.example.juttela.DataSource.Models.GetMessagesRequest
import com.example.juttela.DataSource.Models.GetMessagesResponse
import com.example.juttela.DataSource.Models.GetProfileRequest
import com.example.juttela.DataSource.Models.GetProfileResponse
import com.example.juttela.DataSource.Models.GetRequestsResponse
import com.example.juttela.DataSource.Models.GetSmartConnectionsRequest
import com.example.juttela.DataSource.Models.GetSmartConnectionsResponse
import com.example.juttela.DataSource.Models.GoogleAuthRequest
import com.example.juttela.DataSource.Models.GoogleAuthResponse
import com.example.juttela.DataSource.Models.LocationSessionResponse
import com.example.juttela.DataSource.Models.LocationSessionStartRequest
import com.example.juttela.DataSource.Models.MobileCheckResponse
import com.example.juttela.DataSource.Models.RatingRequest
import com.example.juttela.DataSource.Models.RatingResponse
import com.example.juttela.DataSource.Models.RequestAcceptedResponse
import com.example.juttela.DataSource.Models.RequestModels
import com.example.juttela.DataSource.Models.RequestResponse
import com.example.juttela.DataSource.Models.SendMessageRequest
import com.example.juttela.DataSource.Models.SendMessageResponse
import com.example.juttela.DataSource.Models.SendingSmartRequest
import com.example.juttela.DataSource.Models.SendingSmartRequestResponse
import com.example.juttela.DataSource.Models.SessionSnapshotResponse
import com.example.juttela.DataSource.Models.SignupRequest
import com.example.juttela.DataSource.Models.SignupResponse
import com.example.juttela.DataSource.Models.SmartAcceptRequest
import com.example.juttela.DataSource.Models.SmartAcceptResponse
import com.example.juttela.DataSource.Models.SmartGetRequestBody
import com.example.juttela.DataSource.Models.SmartGetRequestResponse
import com.example.juttela.DataSource.Models.SmartMatchRequest
import com.example.juttela.DataSource.Models.SmartMatchResponse
import com.example.juttela.DataSource.Models.UpdateProfileRequest
import com.example.juttela.DataSource.Models.UpdateProfileResponse
import com.example.juttela.DataSource.Models.UpdateSessionLocationRequest
import com.example.juttela.DataSource.Models.UpdateSessionLocationResponse
import com.example.juttela.DataSource.Models.UserIdRequest

class AuthRepository {

    suspend fun signup(request: SignupRequest): SignupResponse {
        return RetrofitClient.api.signup(request)
    }

    suspend fun getUserById(userId: String): MobileCheckResponse {
        return RetrofitClient.api.GetUserById(
            UserIdRequest(userId)
        )
    }

    suspend fun GeoAddUsersRepo(requestModel: GeoAddRequestModel): GeoAddResponseModel {
        return RetrofitClient.api.GeoAddUsers(
            requestModel
        )
    }

    suspend fun SendRequestRepo(request: RequestModels): RequestResponse {
        return RetrofitClient.api.SendRequest(request)
    }

    suspend fun GetIdResponseRepo(request: GetIdRequest): GetRequestsResponse {
        return RetrofitClient.api.GetRequestData(request)
    }

    suspend fun AcceptRequest(request: AcceptUsersModel): RequestAcceptedResponse {
        return RetrofitClient.api.AcceptRequest(request)
    }

    suspend fun GetConnections(request: ConnectionsRequest): ConnectionsResponse {
        return RetrofitClient.api.GetConnections(request)
    }

    suspend fun SendMessageRepo(request: SendMessageRequest): SendMessageResponse {
        return RetrofitClient.api.SendMessageAPI(request)
    }

    suspend fun GetMessageRepo(request: GetMessagesRequest): GetMessagesResponse {
        return RetrofitClient.api.GetMessages(request)
    }

    suspend fun ProfileUpdateRepo(request: UpdateProfileRequest): UpdateProfileResponse {
        return RetrofitClient.api.ProfileUpdate(request)
    }

    suspend fun GetProfileRepo(request: GetProfileRequest): GetProfileResponse {
        return RetrofitClient.api.GetProfile(request)
    }

    suspend fun SmartMatchRepo(request: SmartMatchRequest): SmartMatchResponse {
        return RetrofitClient.api.SmartMatching(request)
    }

    suspend fun GetSmartRepo(request: SmartGetRequestBody): SmartGetRequestResponse {
        return RetrofitClient.api.GetSmartMatching(request)
    }

    suspend fun SendSmartRequest(request: SendingSmartRequest): SendingSmartRequestResponse {
        return RetrofitClient.api.SmartSendRequest(request)
    }


    suspend fun SmartAcceptRequestRepo(request: SmartAcceptRequest): SmartAcceptResponse {
        return RetrofitClient.api.SmartAcceptRequest(request)
    }


    suspend fun GetSmartConnectionsRequest(request: GetSmartConnectionsRequest): GetSmartConnectionsResponse {
        return RetrofitClient.api.getSmartConnections(request);
    }

    suspend fun SessionsStartRepo(request: LocationSessionStartRequest): LocationSessionResponse {
        return RetrofitClient.api.startSession(request)
    }

    suspend fun AddUserToSessionRepo(
        sessionId: String,
        request: AddUserToSessionRequest): AddUserToSessionResponse {
        return RetrofitClient.api.addUserToSession(
            sessionId = sessionId,
            request = request )
    }

    suspend fun UpdateSessionLocationRepo(
        sessionId: String,
        request: UpdateSessionLocationRequest
    ): UpdateSessionLocationResponse {

        return RetrofitClient.api.updateSessionLocation(
            sessionId = sessionId,
            request = request
        )
    }

    suspend fun GetSessionSnapshotRepo(
        sessionId: String
    ): SessionSnapshotResponse {
        return RetrofitClient.api.getSessionSnapshot(
            sessionId = sessionId
        )
    }

    suspend fun CancelSessionRepo(
        sessionId: String
    ): CancelSessionResponse {
        return RetrofitClient.api.cancelSession(
            sessionId = sessionId
        )
    }

    suspend fun getArrivalsRepo(userId: String): ArrivalsResponse {
        return RetrofitClient.api.getArrivals(userId)
    }


    suspend fun GoogleAuthRepo(request: GoogleAuthRequest) : GoogleAuthResponse{
        return RetrofitClient.api.googleAuth(request)
    }

    suspend fun deleteAccount(userId: String): DeleteAccountResponse {
        return RetrofitClient.api.deleteAccount(userId)
    }

    suspend fun RatingRepo(request : RatingRequest) : RatingResponse{
        return RetrofitClient.api.RatingApi(request)
    }

    suspend fun FeedBackRepo(request: FeedbackRequest) : FeedbackResponse{
        return RetrofitClient.api.FeedbackApi(request)
    }

}