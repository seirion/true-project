## 다트 고유번호 받기 스크립트

[문서](https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019018)

```sh
#!/bin/bash

curl -o corpCode.zip "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=cebe93589e687856da2d84703fbad8ac87f0a98f"

unzip corpCode.zip
rm corpCode.zip
```

결과물이 `CORPCODE.xml`에 저장된다

