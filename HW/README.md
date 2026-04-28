# SMART_DOG_TOY H/W

## 전체 블럭도

## Main B/D 및 USB-C 타입 충전 커넥터 보드
<img src="SMART_DOG_TOY_013.jpg" width="70%"> <img src="SMART_DOG_TOY_012.jpg" width="20%"> <br>

## ???
<img src="SMART_DOG_TOY_001.jpg" width="50%"><br>

## AF25E003456-65E4? : https://github.com/Jieli-Tech
<img src="SMART_DOG_TOY_002.jpg" width="35%"> <img src="SMART_DOG_TOY_011.jpg" width="35%"><br>

## ???
<img src="SMART_DOG_TOY_003.jpg" width="35%"> <img src="SMART_DOG_TOY_010.jpg" width="35%"><br>

## ???
<img src="SMART_DOG_TOY_004.jpg" width="35%"> <img src="SMART_DOG_TOY_005.jpg" width="35%"><br>

## ???
<img src="SMART_DOG_TOY_006.jpg" width="50%"><br>

## MA1616S
  * MX161S는 주로 장난감 자동차나 소형 가전제품에서 바퀴 또는 팬의 회전 방향을 제어하는 데 사용되는 H-브리지(H-Bridge) DC 모터 드라이버 IC
<img src="SMART_DOG_TOY_007.jpg" width="35%"> <img src="SMART_DOG_TOY_008.jpg" width="35%"><br>

### 1. 주요 사양 (Specifications)

* https://makerselectronics.com/product/mx1616-dual-motor-driver-board/?srsltid=AfmBOooqKUp4NMEqXSzG3B44PMonS_U2Infvi9fxXXhpgz5shtlp7TNZ

* MX161S와 전압 범위는 비슷하지만, 채널이 두 개로 늘어난 것이 특징입니다.
  * 동작 전압 Vcc = 2 ~ 10V
  * 연속 출력 전류: 채널당 약 1.3~1.5A
  * 최대 피크 전류: 최대 2.5~3A
  * 보호 기능: 과열 보호 회로(TSD) 내장 


### 2. 16핀 구성 (Pinout) 
일반적인 MX1616 계열 16핀 제품의 핀 배열은 다음과 같습니다 (제조사에 따라 미세한 차이가 있을 수 있으니 패턴 확인 권장): 
| 핀 번호	| 이름	| 설명 | 
|:-----:|:-----:|:-----:|
| 1, 8, 9, 16	| GND	| 공통 접지 (일반적으로 4개 핀이 연결됨) | 
| 2, 3	| IN1, IN2		| 모터 A 제어 입력 (MCU 연결) | 
| 4, 5	| OUT1, OUT2		| 모터 A 출력 (모터 단자 연결) | 
| 6, 7	|  VCC | 모터 및 로직 전원 입력 ()
| 10, 11	| IN3, IN4		| 모터 B 제어 입력 (MCU 연결)
| 12, 13	| OUT3, OUT4		| 모터 B 출력 (모터 단자 연결)
| 14, 15	| VCC | 전원 입력 (내부적으로 6, 7번과 연결된 경우가 많음)

### 3. 회로 구성 가이드
#### 3.1. 전원 연결: 6, 7, 14, 15번 핀(VCC)에 배터리(+)를, 1, 8, 9, 16번 핀(GND)에 배터리(-)를 연결합니다. 전원 안정화를 위해 와 GND 사이에 전해 커패시터를 추가하는 것이 좋습니다.

#### 3.2. 모터 연결:
* 첫 번째 모터는 4, 5번(OUT1, OUT2)에 연결합니다.
* 두 번째 모터는 12, 13번(OUT3, OUT4)에 연결합니다.
* 제어 신호: 아두이노 등의 디지털 핀을 IN1~IN4에 연결하여 방향을 제어합니다. 속도 제어가 필요하다면 PWM 신호를 사용합니다. 

### 3. 제어 로직 (모터 A 예시)
모터 B도 IN3, IN4를 동일한 로직으로 제어하면 됩니다. 
Makers Electronics
Makers Electronics
정회전: IN1 = HIGH, IN2 = LOW
역회전: IN1 = LOW, IN2 = HIGH
브레이크: IN1 = HIGH, IN2 = HIGH (급정지)
대기: IN1 = LOW, IN2 = LOW (자유 회전 정지)


## ???
<img src="SMART_DOG_TOY_009.jpg" width="50%"><br>





