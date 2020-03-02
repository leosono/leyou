package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {

    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String cookieName;

    //对象一旦实例化后就应该获取私钥和公钥
    @PostConstruct
    public void generate() throws Exception {
        //先判断是否有密钥
        File publicKeyFile = new File(pubKeyPath);
        File privateKeyFile = new File(priKeyPath);
        if(!publicKeyFile.exists() || !privateKeyFile.exists()){
            RsaUtils.generateKey
                    (pubKeyPath, priKeyPath, secret);
        }
        //有密钥就直接读取
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }
}
