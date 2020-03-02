package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;
    
    @RequestMapping("/login")
    public ResponseEntity<Void> login
            (@RequestParam("username") String username,
             @RequestParam("password") String password,
             HttpServletRequest request, HttpServletResponse response){

        String token = authService.login(username, password);
        //将token存入cookie
        CookieUtils.setCookie(request, response, prop.getCookieName(),
                token, prop.getExpire(),true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify
            (@CookieValue("LY_TOKEN") String token,
             HttpServletResponse response,
             HttpServletRequest request){
        try{
            UserInfo userinfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //刷新token(创建新的token，存入cookie)
            JwtUtils.generateToken
                    (userinfo, prop.getPrivateKey(), prop.getExpire());
            CookieUtils.setCookie(request, response, prop.getCookieName(),
                    token, prop.getExpire(),true);
            return ResponseEntity.ok(userinfo);
        }catch(Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
