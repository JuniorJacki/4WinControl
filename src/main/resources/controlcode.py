# Copyright (c) JuniorJacki 2025

from pybricks.pupdevices import Motor
from pybricks.parameters import Port
from pybricks.tools import wait
from pybricks.pupdevices import ColorSensor,UltrasonicSensor
from pybricks.hubs import InventorHub
from pybricks.parameters import Stop


# Standard MicroPython modules
import usys


# Define Controls

motor = Motor(Port.D)
motor1 = Motor(Port.C)

hub = InventorHub()
usens = UltrasonicSensor(Port.F)
csens = ColorSensor(Port.E)
smotor = Motor(Port.A)
sradmotor = Motor(Port.B)

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
    sradmotor.run_angle(-800,index*68,Stop.BRAKE,wait=True)
    col = csens.hsv().v
    sradmotor.run_angle(800,index*71,Stop.BRAKE,wait=True)
    return col


def fahrzu(row,ff,minSpeed):
    wanted = 55
    if (row == 0): wanted = 92
    if (row == 1): wanted = 120
    if (row == 2): wanted = 149
    if (row == 3): wanted = 179
    if (row == 4): wanted = 210
    if (row == 5): wanted = 231
    if (row == 6): wanted = 260

    fahrzuDist(wanted,ff,minSpeed)

def fahrzuDist(dist,ff,minSpeed):
    wanted = dist

    dc = 0
    while True:
        distance = usens.distance()
        load = motor1.load() + motor.load()
        if load > 0:
            load = 0

        speed = fullSpeed + (minSpeed-100) - ((-(minSpeed-100)/210)*load)
        #print("R:",wanted,"±",ff,":",speed ,":",load,":"," -> ",distance)


        if distance > 255 and dist > 250 and load < 360:
            movebrake()
            #print("stalled")
            break


        if (distance -ff) < wanted and (distance +ff) > wanted:
            #print(wanted,":",distance)
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

def shukleUnFixed():
    move(70)
    wait(100)
    movebrake()
    move(-70)
    wait(200)
    movebrake()
    move(70)
    wait(100)
    movebrake()

def shukle(distance,ff):
    move(70)
    wait(100)
    movebrake()
    fahrzuDist(distance,ff,50)
    move(-70)
    wait(100)
    movebrake()
    fahrzuDist(distance,ff,50)

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

def getArgs():
    next_byte = usys.stdin.buffer.read(1)
    args = ""
    if next_byte == b':':
        # Read argument until newline or end of input
        arg_data = b""
        while True:
            byte = usys.stdin.buffer.read(1)
            if byte == b'' or byte == b'\n' or byte == b':':
                break
            arg_data += byte
        if arg_data:
            arg_str = str(arg_data)[2:-1]  # Remove b' and ' (e.g., "b'5'" -> "5")
            return arg_str.split('#') if '#' in arg_str else [arg_str] if arg_str else []

def isArgEmpty(args,index):
    if args == None: return True
    return args[index] == b'' or args[index] == ''

def isArgInt(args,index):
    try:
        if not isArgEmpty(args,index):
            int(args[index])
            return True
    except:
        return False
    return False;

while True:
    cmd = usys.stdin.buffer.read(3)
    args = getArgs()
    context = cmd
    if cmd == b"ini":
        context = b"ins:"
    #### Color Sensor Read Commands
    elif cmd == b"col":
        if isArgInt(args,0):
            field = int(args[0])
            if field <= 5 and field >= 0:
                context = b"col:" + str(scanField(int(args[0])))
            else:
                context = b"col:err:" + "IndexErr"
        else:
            context = b"col:err:" + "ArgErr"
    #### Row Move Commands
    elif cmd == b"row":
        if isArgInt(args,0):
            row = int(args[0])
            if row <= 6 and row >= 0:
                context = b"row:" + str(row)
                fahrzu(int(row),1,70)
            else:
                context = b"row:err:" + "IndexErr"
        else:
            context = b"row:err:" + "ArgError"
    elif cmd == b"mtd": # Move to Distance
        if isArgInt(args,0):
            if isArgInt(args,1) and isArgInt(args,2):
                fahrzuDist(int(args[0]),int(args[1]),int(args[2]))
            else:
                fahrzuDist(int(args[0]),1,70)
            context = b"mtd:" + str(args[0])
        else:
            context = b"row:err:" + "ArgError"
    elif cmd == b"dst": # DisplayText
        if not isArgEmpty(args,0):
            hub.display.text(args[0])
        else:
            context = b"dst:err:" + "NoArgErr"
    #### Throw Commands
    elif cmd == b"thr":
        throw()
        context = b"thr:"
    #### Other
    elif cmd == b"shk":
        if isArgInt(args,0):
            shukle(int(args[0]),1)
            context = b"shk:fixed:"
        else:
            context = b"shk:unfixed:"
            shukleUnFixed()
    elif cmd == b"res":
        context = b"res:" + str(checkAusrichtung())
    elif cmd == b"bye":
        break
    elif cmd == b"dat":
        context = None
    else:
        context = cmd + ":err:NotFound"

    if not context == None:
        usys.stdout.buffer.write(b"ret:" + context)
        wait(100)
    usys.stdout.flush()



