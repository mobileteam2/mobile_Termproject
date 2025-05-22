# git branch push 방법

``` bach
git clone https://github.com/mobileteam2/mobile_Termproject.git
```
현재 파일 경로에 해당 레포지토리 불러오기

```bash
git add .
```
레포지토리 변경사항 저장. 
또는

``` bash
git add exampleFile.py
```
이 처럼 특정 파일의 변경사항만 저장가능.

``` bash
git status
```
`git add`로 추가한 변경사항 확인 


```bash
git commit -m "커밋내용"
```
`git add`로 인한 변경사항 git에 커밋.

## 중요.
```bash
git pull
```
git에 업로드 하기 전, 레포지토리 최신버전 로드. 
`중요` -> git 충돌 방지 -> 이래야 안번거로

```bash
git push origin [브랜치 이름]
```
해당 `브랜치 이름`에 업로드

`git push`일 시 자동적으로 기본 브랜치로 push.


움
