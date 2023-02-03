package zn;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import zn.model.AuthConfig;
import zn.model.TokenResponse;

import java.util.HashMap;
import java.util.Map;


public class JWTUtil
{
  private Decoder b64dec = Base64.getDecoder();
  private Encoder b64enc = Base64.getEncoder();

  private Decoder b64Urldec = Base64.getUrlDecoder();
  private Encoder b64Urlenc = Base64.getUrlEncoder();

  public JWTUtil()
  {

  }

  public String getToken(AuthConfig authConfig) throws Exception
  {
    String authorization=new String(b64enc.encode(String.format("%s:%s", authConfig.getClientId(), authConfig.getClientSecret()).getBytes()));
    
    Http http=new Http();
    TokenResponse tokenResponse=null;
    if(authConfig.getClientAssertionType()==null)
    {
      http.post(authConfig.getTokenUrl(), String.format("grant_type=%s&scope=%s", authConfig.getGrantType(), authConfig.getScope()))
          .withHeader("content-type", "application/x-www-form-urlencoded")
          .withHeader("authorization", "Basic " + authorization);
    }
    else
    {
      String assertionJWT=createJWT(authConfig.getClientId(), authConfig.getClientId(), authConfig.getAud(), authConfig.getKid(), null, authConfig.getPrivateKey());
      String postPayload=String.format("grant_type=%s&scope=%s&client_id=%s&client_assertion_type=%s&&client_assertion=%s", 
                                        authConfig.getGrantType(), authConfig.getScope(), authConfig.getClientId(), authConfig.getClientAssertionType(), assertionJWT);
      http.post(authConfig.getTokenUrl(), postPayload)
          .withHeader("content-type", "application/x-www-form-urlencoded");
    }
    tokenResponse=http.$(TokenResponse.class);
    return tokenResponse.getAccessToken();
  }

  public String createJWT(String sub, String iss, String aud, String kid, Map<String, Object> extra, String privateKeySpec) throws Exception
  {
    Map<String, Object> header=new HashMap<String, Object>();
    header.put("alg", "RS256");
    header.put("typ", "JWT");
    if(kid!=null) header.put("kid", kid);
    
    long now=(System.currentTimeMillis()/1000);
    int expiry=3600;

    Map<String, Object> payload=new HashMap<String, Object>();
    if(extra!=null) payload.putAll(extra);
    payload.put("sub", sub);
    payload.put("iss", iss);
    payload.put("aud", aud);
    payload.put("iat", now);
    payload.put("exp", now+expiry);
    
    String encodedHeader=new String(b64Urlenc.encode(Json.stringify(header).getBytes()));
    String encodedPayload=new String(b64Urlenc.encode(Json.stringify(payload).getBytes()));
    
    String data=encodedHeader + "." + encodedPayload;

    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b64dec.decode(privateKeySpec));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PrivateKey privateKey=keyFactory.generatePrivate(spec);
              
    Signature privateSignature = Signature.getInstance("SHA256withRSA");
    privateSignature.initSign(privateKey);
    privateSignature.update(data.getBytes());

    String signature = b64Urlenc.encodeToString(privateSignature.sign());
  
    return data + "." + signature;
  }

  public String verifyToken(String token, String keySpec) throws Exception
  {
    String[] parts=token.split("\\.");
    if(parts.length!=3) return "INVALID-TOKEN-SEGMENTS";

    Map<?,?> payloadDecoded=Json.parseJson(new String(b64Urldec.decode(parts[1])), Map.class);
    long exp=((Number)payloadDecoded.get("exp")).longValue();
    long now=(System.currentTimeMillis()/1000);
    if(now>exp) return "TOKEN-EXPIRED";
    
    byte[] decodedSig=b64Urldec.decode(parts[2]);

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(b64dec.decode(keySpec)));
    PublicKey publicKey = cert.getPublicKey();

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(publicKey);
    sig.update((parts[0]+"."+parts[1]).getBytes());

    if(sig.verify(decodedSig)) return "TOKEN-VALID";
    return "TOKEN-INVALID";
  }

  public Map<?,?> decodeToken(String token)
  {
    String[] parts=token.split("\\.");
    Map<?,?> payloadDecoded=Json.parseJson(new String(b64Urldec.decode(parts[1])), Map.class);
    return payloadDecoded;
  }

  public static void main(String[] args) throws Exception
  {
    JWTUtil jwtutil=new JWTUtil();
    String token="eyJ4NXQjUzI1NiI6IlZSY0gtTlVKbGtEdS1pZ2N4dTZESnM5VEZkMFQ4Y3VvQ1VnWldoY1BoVVUiLCJ4NXQiOiJnSl9uX0tYZ0l0Nm1fMWpMTWJtRkF5dEI3b3ciLCJraWQiOiJTSUdOSU5HX0tFWSIsImFsZyI6IlJTMjU2In0.eyJ1c2VyX3R6IjoiQW1lcmljYVwvQ2hpY2FnbyIsInN1YiI6IlByYWJoYWthcmFuLlRoYW5hcGFsQGNvZ25pemFudC5jb20iLCJ1c2VyX2xvY2FsZSI6ImVuIiwic2lkbGUiOjQ4MCwiaWRwX25hbWUiOiJVc2VyTmFtZVBhc3N3b3JkIiwidXNlci50ZW5hbnQubmFtZSI6ImlkY3MtMWRmZGRiMjc5MjY0NGU1ZGFkZjM2MGMzYmMzODE1MTIiLCJpZHBfZ3VpZCI6IlVzZXJOYW1lUGFzc3dvcmQiLCJhbXIiOlsiVVNFUk5BTUVfUEFTU1dPUkQiXSwiaXNzIjoiaHR0cHM6XC9cL2lkZW50aXR5Lm9yYWNsZWNsb3VkLmNvbVwvIiwidXNlcl90ZW5hbnRuYW1lIjoiaWRjcy0xZGZkZGIyNzkyNjQ0ZTVkYWRmMzYwYzNiYzM4MTUxMiIsImNsaWVudF9pZCI6ImQ0NjAyY2E1MDIzMTQzNDJhYzgxMzhiZDczYTcwODg3Iiwic2lkIjoiZjhjZWEyYTVhYTk0NDE0MmEzNzYzZDJhYWI0ZjRmM2I6OTQ5NjgzIiwic3ViX3R5cGUiOiJ1c2VyIiwic2NvcGUiOiJwaG9uZSBvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImNsaWVudF90ZW5hbnRuYW1lIjoiaWRjcy0xZGZkZGIyNzkyNjQ0ZTVkYWRmMzYwYzNiYzM4MTUxMiIsInJlZ2lvbl9uYW1lIjoiZXUtZnJhbmtmdXJ0LWlkY3MtMiIsInVzZXJfbGFuZyI6ImVuIiwiZXhwIjoxNjUzODQxOTA5LCJpYXQiOjE2NTM4MzgzMDksImNsaWVudF9ndWlkIjoiZjc2ZWFhZjQ3YzQ3NDE1Yjk1NDNmZjI3NTVlNmYzZmEiLCJjbGllbnRfbmFtZSI6IkFuZ3VsYXJJRENTQXBwIiwiaWRwX3R5cGUiOiJMT0NBTCIsInRlbmFudCI6ImlkY3MtMWRmZGRiMjc5MjY0NGU1ZGFkZjM2MGMzYmMzODE1MTIiLCJqdGkiOiIzYzMyOGE1ZjczODY0ZjI4YmUwNmE1NWM4MjZhNTMzNSIsImd0cCI6ImF6YyIsInVzZXJfZGlzcGxheW5hbWUiOiJQcmFiaGFrYXJhbiBUaGFuYXBhbCIsIm9wYyI6ZmFsc2UsInN1Yl9tYXBwaW5nYXR0ciI6InVzZXJOYW1lIiwicHJpbVRlbmFudCI6dHJ1ZSwidG9rX3R5cGUiOiJBVCIsImNhX2d1aWQiOiJjYWNjdC00MTIxY2Q1ZmUyOTI0MjU5OWY3MWVlN2VmNThmOGM0NSIsImF1ZCI6Imh0dHBzOlwvXC9pZGNzLTFkZmRkYjI3OTI2NDRlNWRhZGYzNjBjM2JjMzgxNTEyLmlkZW50aXR5Lm9yYWNsZWNsb3VkLmNvbSIsInVzZXJfaWQiOiJhZGYzMjBmNWRhM2I0NzViYWRmNGI5NzA1MzcwZWYyZiIsInRlbmFudF9pc3MiOiJodHRwczpcL1wvaWRjcy0xZGZkZGIyNzkyNjQ0ZTVkYWRmMzYwYzNiYzM4MTUxMi5pZGVudGl0eS5vcmFjbGVjbG91ZC5jb20ifQ.PWVEwsATp9rpWrDz5mL9JcxbKov7uWwceYpfQt__J2koINNk_oila9DdSRiV5xENHCILB8CIkA5027UgqLYg8Ucg62up5F9p39JzwwXgexVP8q_fNinqUCoVAs24xMGr8Rtq37OfKh0QFD18LDBRz5l7rlI3qJ0v-YiwtCHHgLOowKenD3MolLT-x3cDntvr5CKD8SG-FtVkWRUMZWNWq9tbW3JRtNLiCIcvjT-swKMTAqa-zdEfNcfVV697bMWF3NZE4ycpapL8gTdPFLgb6rVEVNbRAEi6z9am1Bp6hiXs5cEo8rclAHETQnCRNVCyPqO_kcHtQWmzxf7ZsBitmQ";
    String keySpec="MIIDYTCCAkmgAwIBAgIGAWTRy6WvMA0GCSqGSIb3DQEBCwUAMFkxEzARBgoJkiaJk/IsZAEZFgNjb20xFjAUBgoJkiaJk/IsZAEZFgZvcmFjbGUxFTATBgoJkiaJk/IsZAEZFgVjbG91ZDETMBEGA1UEAxMKQ2xvdWQ5Q0EtMjAeFw0xODA3MjUxNDE1NDFaFw0yODA3MjUxNDE1NDFaMFYxEzARBgNVBAMTCnNzbERvbWFpbnMxDzANBgNVBAMTBkNsb3VkOTEuMCwGA1UEAxMlaWRjcy0xZGZkZGIyNzkyNjQ0ZTVkYWRmMzYwYzNiYzM4MTUxMjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJo8FBiF0P0QchR6LWw1GMok4r7AhkrsNsdeiUrHayqQCVDfbF789QbbHGx4glK4fiGVzWRrhwyh6E/7yI4h4IYVeekSu+bpCAJd5HRuJUURBc+y7C7s+PtOHzprD9m+/+IX/4K2Fum2HVoeFcglGpz0S5KSfGNrXYQpo4v7jZQEDkx0c3H9Xw/7ELd9sv+5le5MGC+THXEWs+vmh9Oir+c/E/MbRFgwsiniwNWUQ5v/0XkPH2T9CC3Pi7LaFbvgQdAoxRDfjS1fAM8VLYfMcNfCYj/4i7eK/UCm8RSWWFXYmfUDTS7leuFeBfVG/SWdXyoYgXQCLuGWBlj8lLwIPjsCAwEAAaMyMDAwDwYDVR0PAQH/BAUDAwf4ADAdBgNVHQ4EFgQUudJrJBXoa/ePfjq7le86wWGyiq4wDQYJKoZIhvcNAQELBQADggEBALpaZogRKJpnWfbKCo0k0J9pOq0mCNf7MQqirpJB139XgxqhBT3hYPcMTg8c9vsKYKhsfDHvrhN/S2FXlGZPND7tXYiUj10pUrodzRtZQAHKoLB38wz6NNhSNgY0HmtCWZAKDoluoTMoQ4YVSICDXT9I2K0Gw+TSUT/jiHMjaeKfbai2potfCjTZcTrZidk1F4lbR4f30/6ZAP/ldVF55tficz0+1a944pU1kh9XwfkmCxgtWPPLhgfHJi0glQwJMBU9LOhf7tTrRjQbhR/UzGqLubNd1r62F8ud5CcwZFVOipdHLmCuxQ9VncfoaESjThE6MpMF9mmPdNgAaLKXMx4=";

    System.out.println(jwtutil.verifyToken(token, keySpec));
    // String[] parts=token.split("\\.");

    // String headerDecoded=new String(jwtutil.b64dec.decode(parts[0]));
    // Map header=Json.parseJson(headerDecoded, Map.class);

    // String payloadDecoded=new String(jwtutil.b64dec.decode(parts[1]));
    // Map payload=Json.parseJson(payloadDecoded, Map.class);

    // System.out.println(header+"\n");
    // System.out.println(payload+"\n");

    // byte[] decodedSig=jwtutil.b64dec.decode(parts[2]);


    // CertificateFactory factory = CertificateFactory.getInstance("X.509");
    // X509Certificate cert1 = (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
    // PublicKey publicKey = (RSAPublicKey)cert1.getPublicKey();
        
    // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(cert));
    // KeyFactory kf = KeyFactory.getInstance("RSA");
    // PublicKey publicKey = kf.generatePublic(keySpec);

    // Signature sig = Signature.getInstance("SHA256withRSA");
    // sig.initVerify(publicKey);
    // sig.update((parts[0]+"."+parts[1]).getBytes());

    // System.out.println(sig.verify(decodedSig));
   
  }
}