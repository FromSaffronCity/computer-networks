# instantiating simulator
set ns [new Simulator]

# defining options
set val(rp) DSDV  ;# ad-hoc routing protocol
set val(ll) LL  ;# data-link-layer type
set val(mac) Mac/802_11  ;# wireless MAC protocol type
set val(prop) Propagation/TwoRayGround  ;# radio-propagation model
set val(ifq) Queue/DropTail/PriQueue  ;# interface queue type
set val(ifqlen) 50  ;# max packet in ifq
set val(netif) Phy/WirelessPhy  ;# network interface type
set val(ant) Antenna/OmniAntenna  ;# antenna type
set val(chan) Channel/WirelessChannel  ;# channel type

set val(as) [lindex $argv 0]  ;# area side
set val(nn) [lindex $argv 1]  ;# number of mobilenodes
set val(nf) [lindex $argv 2]  ;# number of flows

# opening trace file
set trace_file [open trace.tr w]
$ns trace-all $trace_file

# opening nam file
set nam_file [open animation.nam w]
$ns namtrace-all-wireless $nam_file $val(as) $val(as)

# instantiating network topology to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(as) $val(as)  ;# $val(as)m x $val(as)m area

# creating general operation director for mobilenodes
create-god $val(nn)

# configuring node
$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -propType $val(prop) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -phyType $val(netif) \
                -antType $val(ant) \
                -channelType $val(chan) \
                -topoInstance $topo \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

# creating network-layer nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    set node($i) [$ns node]

    # disabling random motion
    $node($i) random-motion 0

    # positioning node randmoly along x & y axes
    $node($i) set X_ [expr int(10000 * rand()) % $val(as) + 0.5]
    $node($i) set Y_ [expr int(10000 * rand()) % $val(as) + 0.5]
    $node($i) set Z_ 0

    # configuring node
    $ns initial_node_pos $node($i) 20
}

# producing node movements with uniform random speed
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at [expr int(20 * rand()) + 10] "$node($i) setdest [expr int(10000 * rand()) % $val(as) + 0.5] [expr int(10000 * rand()) % $val(as) + 0.5] [expr int(100 * rand()) % 5 + 1]"
}

# generating traffic/flow
# picking random source node
set src [expr int(1000 * rand()) % $val(nn)]

for {set i 0} {$i < $val(nf)} {incr i} {
    # picking random destination/sink node
    while {$src == $src} {
        set dest [expr int(1000 * rand()) % $val(nn)]
        if {$src != $dest} {
            break
        }
    }

    # configuring traffic/flow
    # creating transport-layer agents
    set udp [new Agent/UDP]
    set null [new Agent/Null]

    # attaching agents to nodes
    $ns attach-agent $node($src) $udp
    $ns attach-agent $node($dest) $null

    # connecting agents
    $ns connect $udp $null

    # marking flow
    $udp set fid_ $i

    # creating application-layer traffic/flow generator
    set cbr [new Application/Traffic/CBR]

    # attaching traffic/flow generator to agent
    $cbr attach-agent $udp

    # configuring traffic/flow generator
    $cbr set type_ CBR
    $cbr set packet_size_ 1000
    $cbr set rate_ 1mb
    $cbr set random_ false

    # starting traffic/flow generation
    $ns at [expr int(9 * rand()) + 1] "$cbr start"
}

# ending simulation
# stopping nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# defining terminating procedures
proc finish_simulation {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "ending simulation"
    $ns halt
}

# calling terminating procedures
$ns at 50.0001 "finish_simulation"
$ns at 50.0002 "halt_simulation"

# running simulation
puts "starting simulation"
$ns run
