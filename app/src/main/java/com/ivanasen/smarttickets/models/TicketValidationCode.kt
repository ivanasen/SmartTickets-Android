package com.ivanasen.smarttickets.models

import org.web3j.crypto.Sign

data class TicketValidationCode(val ticket: String,
                                val ticketSignature: Sign.SignatureData?,
                                val address: String)