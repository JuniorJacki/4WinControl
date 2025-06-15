from pybricks.pupdevices import Motor
from pybricks.parameters import Port
from pybricks.tools import wait
from pybricks.pupdevices import ColorSensor,UltrasonicSensor
from pybricks.hubs import InventorHub
from pybricks.parameters import Stop

# Standard MicroPython modules
from usys import stdin, stdout
from uselect import poll

# Define Controls

motor = Motor(Port.D)
motor1 = Motor(Port.C)

hub = InventorHub()
usens = UltrasonicSensor(Port.A)
csens = ColorSensor(Port.B)
smotor = Motor(Port.F)
sradmotor = Motor(Port.E)

# Settings
fullSpeed = 100
motor.settings(9000)
motor.control.limits(torque=1000,speed=2000)

def move(speed):
    motor.dc(speed)
    motor1.dc(speed)

def movebrake():
    motor.brake()
    motor1.brake()

def throw():
    sradmotor.run_time(-500,800,Stop.BRAKE,False)
    wait(800)
    smotor.run_time(-150,1000,Stop.BRAKE,False)
    wait(500)
    sradmotor.run_time(500,800,Stop.BRAKE,False)
    wait(500)
    smotor.run_time(150,1000,Stop.BRAKE,True)

def scanField(index):
    sradmotor.run_angle(-500,index*65,Stop.BRAKE,wait=True)
    col = csens.hsv().v
    sradmotor.run_angle(500,index*69,Stop.BRAKE,wait=True)
    return col


def fahrzu(row,ff):
    wanted = 55
    if (row == 0): wanted = 92
    if (row == 1): wanted = 120
    if (row == 2): wanted = 149
    if (row == 3): wanted = 179
    if (row == 4): wanted = 210
    if (row == 5): wanted = 231
    if (row == 6): wanted = 280

    dc = 0
    while True:
        distance = usens.distance()
        load = motor1.load() + motor.load()
        if load > 0:
            load = 0
        speed = fullSpeed - 30 - ((30/210)*load)
        print("R:",row,wanted,"±",ff,":",speed ,":",load,":"," -> ",distance)

        if (distance -ff) < wanted and (distance +ff) > wanted:
            print(wanted,":",distance)
            movebrake()
            break
        if distance == 2000:
            move(speed)
            wait(150)
            motor.dc(-speed)
        if distance < (wanted - ff):
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        else:
            speed = -speed
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        wait(10)

def shukle():
    move(70)
    wait(200)
    movebrake()
    move(-70)
    wait(200)
    movebrake()

def checkAusrichtung():
    movebrake()
    move(-80)
    while(True):
        if (motor.stalled()):
            break
    movebrake()
    return usens.distance()

## Reset to Standard Position
checkAusrichtung()


keyboard = poll()
keyboard.register(stdin)

context = b"pss" # Programm Successfully Started

while True:
    stdout.buffer.write(b"rdy:" + context)
    context = ""
    while not keyboard.poll(0):
        wait(10)

    cmd = stdin.buffer.read(3)
    if cmd == b"ini":
        context = b"ins:"
    #### Color Sensor Read Commands
    elif cmd == b"co0":
        context = b"co0:" + str(scanField(0))
    elif cmd == b"co1":
        context = b"co1:" + str(scanField(1))
    elif cmd == b"co2":
        context = b"co2:" + str(scanField(2))
    elif cmd == b"co3":
        context = b"co3:" + str(scanField(3))
    elif cmd == b"co4":
        context = b"co4:" + str(scanField(4))
    elif cmd == b"co5":
        context = b"co5:" + str(scanField(5))
    #### Row Move Commands
    elif cmd == b"ro0:":
        context = b"ro0:"
        fahrzu(0,1)
    elif cmd == b"ro1:":
        context = b"ro1:"
        fahrzu(1,1)
    elif cmd == b"ro2:":
        context = b"ro2:"
        fahrzu(2,1)
    elif cmd == b"ro3:":
        context = b"ro3:"
        fahrzu(3,1)
    elif cmd == b"ro4:":
        context = b"ro4:"
        fahrzu(4,1)
    elif cmd == b"ro5:":
        context = b"ro5:"
        fahrzu(5,1)
    elif cmd == b"ro6:":
        context = b"ro6:"
        fahrzu(6,1)
    #### Throw Commands
    elif cmd == b"thr":
        throw()
        context = b"thr:"
    #### Other
    elif cmd == b"shk":
        shukle()
    elif cmd == b"res":
        context = b"res:" + str(checkAusrichtung())
    elif cmd == b"bye":
        break
    else:
        context = cmd


