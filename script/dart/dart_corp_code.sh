#!/bin/bash

curl -o corpCode.zip "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=cebe93589e687856da2d84703fbad8ac87f0a98f"

unzip corpCode.zip
rm corpCode.zip
