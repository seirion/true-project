### github actions release 스크립트 작성

#### jks 파일을 base64 인코딩으로 변환

```sh
openssl base64 -in my_keystore.jks -out base64.txt
```

이것을 github secret 에 등록하고
github actions 에서 다음과 같이 활용할 수 있다

```yaml
    - name: Generate Keystore file from Github Secrets
      env:
        KEYSTORE: ${{ secrets.UPLOAD_KEYSTORE_JKS_BASE64 }}
      run: |
        echo "$KEYSTORE" > ./keystore.b64
        base64 -d -i ./keystore.b64 > ./app/my_keystore.jks
```




#### playstore 에 결과물 업로드를 위한 service account 생성

[링크](https://developers.google.com/android-publisher/getting_started?hl=ko#service-account)
