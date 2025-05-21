# Splitscreen on Servers

Controlify Splitscreen has server-side support.

It allows the host of a splitscreen session to use their Minecraft account to authenticate *all* players in the session.

## Login protocol

The following diagram shows the login phase. The blue sections indicate the modded, splitscreen part of the protocol.

```mermaid
sequenceDiagram
    actor C1 as Client1 (Controller)
    participant Server

    rect blue
    create actor C2 as Client2 (Pawn1)
    C1-->>C2: Spawn second MC process: Give MC account UUID
    end

    Note over C1: Join server

    C1->>Server: HelloPacket(username1, uuid1)

    rect blue
    Server->>C1: SplitscreenIdentifyPacket(protocolVersion)
    C1->>Server: SplitscreenIdentifyPacket(Controller(pawnCount, clientConfig))
    Note over Server: Verify (pawnCount <= MAX_PAWN_COUNT)
    end
    Server->>C1: HelloPacket(encryptionChallenge1, shouldAuthenticate: true)
    C1->>Server: KeyPacket(secret1, public1, challenge1)
    Note over Server,C1: Enable encryption

    create participant Mojang as Mojang Servers
    Server->>Mojang: Authenticate (username1, digest1, serverAddress)
    destroy Mojang
    Mojang->>Server: Authentication success
    Server->>C1: LoginCompressionPacket(threshold)
    Note over Server,C1: Enable compression

    rect blue
    Note over Server: Generate splitscreen nonce
    Server->>C1: NoncePacket(nonce)

    C1-->>C2: JoinServerPacket(host, port, nonce)
    end

    C2->>Server: HelloPacket(username2, uuid: null)

    rect blue
    Server->>C2: SplitscreenIdentifyPacket(protocolVersion)
    Note over C2: hmac = HMAC_SHA256(buf[controllerUuid, pawnIndex], nonce)
    C2->>Server: SplitscreenIdentifyPacket(Pawn(controllerUuid, hmac, pawnIndex))
    Note over Server: Reproduce and verify HMAC<br/>Validate pawn index is within accepted range of MAX_PAWN_COUNT<br/>Ensure requested username is of correct format: "${username1}.${pawnIndex}"<br/>Ensure this isn't a duplicate login
    end
    Server->>C2: HelloPacket(encryptionChallenge2, shouldAuthenticate: false)
    C2->>Server: KeyPacket(secret2, public2, challenge2)
    Note over Server,C2: Enable encryption
    Server->>C2: LoginCompressionPacket(threshold)
    Note over Server,C2: Enable compression

    Server->>C1: LoginFinishedPacket(profile1)
    Server->>C2: LoginFinishedPacket(profile2)
    C1->>Server: LoginAckPacket()
    Note over Server,C1: Switch to CONFIGURATION protocol
    C2->>Server: LoginAckPacket()
    Note over Server,C2: Switch to CONFIGURATION protocol
```
