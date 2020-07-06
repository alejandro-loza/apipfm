package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.ClientProfile

@Service(ClientProfile)
interface ClientProfileGormService {
    ClientProfile save( ClientProfile clientProfile )
}
