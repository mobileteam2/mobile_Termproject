1. [git 사용법](#git-branch-push-방법)
2. [변경 사항](#변경사항)
3. [각 파일 설명](#파일-설명)


# git branch push 방법

``` bach
git clone https://github.com/mobileteam2/mobile_Termproject.git
```
현재 파일 경로에 해당 레포지토리 불러오기

- 레포지토리 변경사항 저장. 
```bash
git add .
```

- 특정 파일의 변경사항만 저장
``` bash
git add exampleFile.py
```

- `git add`로 추가한 변경사항 확인 
``` bash
git status
```

- `git add`로 인한 변경사항 git에 커밋.
```bash
git commit -m "커밋내용"
```
git 내 커밋 로그에 저장됨. `push` 전까진 github 레포지토리에 할당 X

## 중요.
- git에 업로드 하기 전, git 최신버전으로 업데이트. 
```bash
git pull
```
`중요` -> git 충돌 방지, 다른 사람과 git push 내역이 충돌을 일으킬 수 있음.

- 해당 `브랜치 이름`에 업로드
```bash
git push origin [브랜치 이름]
```

- 자동으로 현재 브랜치에 업로드
``` bash
git push
```


# 변경사항 
![Image](https://github.com/user-attachments/assets/dedd74f1-9526-42c1-8ef3-bcfbbce92789)

앱 상단, 하단 바 `menu/ .xml` 로 처리.


# 파일 설명
- `server.py`:
    파이썬 flask 서버 실행 파일. 웹 크롤링 기능 수행. 
    서버와 상호작용 필요 시, 여기에 함수 추가.

- `ngrok.exe`:
    파이썬 flask 서버 실행 IP를 도메인으로 할당하여, 같은 네트워크 상에 있지 않아도 연결 가능하게 함.