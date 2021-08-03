package com.iffly.rtcchat

enum class CallState {
    Idle, Outgoing, Incoming, Connecting, Connected
}

enum class CallEndReason {
    Busy, SignalError, RemoteSignalError, Hangup, MediaError, RemoteHangup, OpenCameraFailure, Timeout, AcceptByOtherClient
}

enum class RefuseType {
    Busy, Hangup
}