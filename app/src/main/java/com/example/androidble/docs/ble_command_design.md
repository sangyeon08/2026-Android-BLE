### BLE 명령 송수신 설계

통신 방식: 보낼 때 RX write, 받을 때는 TX notify

---

#### 1. 한 줄 요약

모든 명령은 "ScanViewModel" 의 "sendCommand(BleCommand)" 로 보내고,<br>
응답은 "BleConnection" 의 "onCharacteristicChanged()" 에서 받아 "onResponse()" 로 넘김

---

#### 2. 명령어 목록

| 명령 | 기능 | DATA | 보내는 함수 | 받는 함수 |
|---|---|---|---|---|
| DS | 단위, 부저음 설정 | 단위 1byte + 부저음 1byte | sendCommand(BleCommand("DS", data)) | onResponse() |
| AR | 연결 확인 | 없음  | sendCommand(BleCommand("AR")) | onResponse() |
| RS | 측정값 요청 | 없음  | sendCommand(BleCommand("RS")) | onResponse() |
| HC | 측정값 초기화 | 없음  | sendCommand(BleCommand("HC")) | onResponse() |
| PS | 전원 끄기 | 없음 | sendCommand(BleCommand("PS")) | onResponse() |
| MM | 측정 모드 설정 | 모드 1byte | sendCommand(BleCommand("MM", data)) | onResponse() |

(BOD는 명령어 두글자 + 데이터의 바이트 크기. 64가 오버되면 넘기기!)
(CRC는 다 더해서 마찬가지로 64 기준으로 넘기기.)

- DATA 값은 숫자가 아니라 ASCII 문자. '0'은 0x30, '1'은 0x31 (0x00, 0x01 아님)
- DS 예시는 단위 '0'(kg), 부저음 '0'(Off) 기준. 단위 '1'은 lbs, 부저음 '1'은 On
- MM 예시는 '0'(최고값으로 측정값 확정) 기준
- ByteArray는 전부 BleCommand(cmd, data).toByteArray()로

BleConnection.write()의 반환값 (Boolean?)

| 값 | 의미 |
|---|---|
| null | 연결 안 됨, 또는 서비스/캐릭터리스틱 없음 |
| false | write 실패 |
| true | 전송 성공 |

- true는 "보냈다"는 뜻이지 악력계가 받았다는 뜻은 아님. 받았는지는 onResponse()로 확인

---

#### 3. 흐름

보내기

```
ScanScreen (버튼)
  → ScanViewModel.sendCommand(BleCommand("AR"))
    → BleCommand.toByteArray()
      → BleConnection.write()
        → 악력계 (RX)
```

받기

```
악력계 (TX)
  → BleConnection.onCharacteristicChanged()
    → onResponse(bytes)
```

- GripCommandInterface는 ScanViewModel이 구현
- BleConnection은 받은 바이트를 Flow로 내보내고, ScanViewModel이 그걸 수집해서 onResponse(bytes)를 호출
---

#### 4. 응답

- 응답은 명령 종류와 상관없이 다 TX
- 받은 바이트의 CMD 두 글자를 보고 어떤 명령의 응답인지 구분
- RS만 응답에 DATA가 있고, 나머지는 DATA 없이 같은 CMD가 돌아옴
- 명령은 한 번에 하나만. 송신 Queue는 만들지 않음
- 측정값은 RS 응답의 측정 상태가 '1'(측정 완료)이 아니면 응답을 받은 뒤 RS를 다시 보냄. 명령어 그대로 돌려주기.


RS 응답 DATA

| 측정 상태 | 의미 | 뒤에 오는 값 |
|---|---|---|
| '0' | 측정 중 | 최대 측정값 |
| '1' | 측정 완료 | 최대 측정값 |
| '2' | 에러 | 에러 코드 4byte |

- 구성: 측정 상태(1byte) + 구분자(0x1b) + 값 + 구분자(0x1b)
- 측정값은 소수점을 뺀 숫자 문자열 (23.5kg이면 "235")
