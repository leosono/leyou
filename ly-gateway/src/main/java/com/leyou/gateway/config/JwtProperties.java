package com.leyou.gateway.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {

    private String pubKeyPath;
    private PublicKey publicKey;
    private String cookieName;

    //对象一旦实例化后就应该获取私钥和公钥
    @PostConstruct
    public void generate() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
