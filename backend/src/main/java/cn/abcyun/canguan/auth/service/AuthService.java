package cn.abcyun.canguan.auth.service;

import cn.abcyun.canguan.auth.web.AuthWebModels;

public interface AuthService {

    AuthWebModels.LoginResponse login(AuthWebModels.LoginRequest request);

    AuthWebModels.LoginResponse refresh(AuthWebModels.RefreshRequest request);

    void logout(AuthWebModels.LogoutRequest request);
}
