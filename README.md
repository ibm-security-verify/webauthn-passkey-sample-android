# IBM Security Verify Passkey Sample App project

An implementation of Apple Passkeys with IBM Security Verify as the relying party FIDO service.

## Getting started

The resource links in the prerequisites explain and demonstrate how you create a new tenant application and configure the security settings to enable FIDO to be used in the sample app.

### Prerequisites

- Getting started

> See [Before you begin](https://github.com/ibm-security-verify/webauthn-relying-party-server-swift/blob/main/README.md)

## Getting started
1. Open Terminal and clone the repository and open the project folder in Android Studio.
   ```
   git clone https://github.com/ibm-security-verify/webauthn-passkey-sample-android.git
   ```

2. Ensure an `assetlinks.json` file is present on your domain in the `.well-known` directory, and that it contains an SHA256 hash of the signing key for your app. For example:
     ```
{
    "relation": [
      "delegate_permission/common.handle_all_urls",
      "delegate_permission/common.get_login_creds"
    ],
    "target": {
      "namespace": "android_app",
      "package_name": "com.ibm.security.passkeydemo",
      "sha256_cert_fingerprints": [
        "1D:17:1A:FF:B2:01:DC:69:3D:44:D1:68:17:41:57:43:B4:B1:FC:C5:65:F9:0C:C2:B9:F1:AF:5A:E4:87:2F:1F"
      ]
    }
  }
    ```

3. Update the `keystore.properties` file with your signing information.

4. The SHA1 hash of the app needs to be added as an `ogirin` to the server configuarion. That value should look similar to this: `android:apk-key-hash:HRca_7IB3Gk9RNFoF0FXQ7Sx_MVl-QzCufGvWuSHLx8`

5. Replace the **SERVER** variable in `Constants.kt` with the host name of the relying party. 


## Resources
Get hash of signing keys: `./gradlew signingReport`

[W3C Web Authentication](https://www.w3.org/TR/webauthn-2/)
