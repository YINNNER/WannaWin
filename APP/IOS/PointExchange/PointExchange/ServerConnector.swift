//
//  ServerConnector.swift
//  PointExchange
//
//  Created by panyy on 2018/7/8.
//  Copyright © 2018年 WannaWin. All rights reserved.
//

import UIKit
import Moya
import SwiftyJSON
import Alamofire

class ServerConnector: NSObject {
    
    static var provider: MoyaProvider<ServerService>{
        //自定义manager
        let securityPolicy:[String:ServerTrustPolicy]=[
            "193.112.44.141":.disableEvaluation
        ]
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 10
        let manager = Manager(configuration: config,serverTrustPolicyManager:ServerTrustPolicyManager(policies: securityPolicy))
//        let manager = Manager(configuration: config)
        
        return MoyaProvider<ServerService>(manager:manager,plugins: [NetworkLoggerPlugin(verbose: true)])
    }
    
    // 账户
    /// 获取验证码
    static func getVCode(phoneNumber:String){
        provider.request(.getVCode(phoneNumber:phoneNumber)){result in
            if case .success(_) = result {
                print("发送验证码成功")
            }
        }
        
    }
    /// 提交密码并验证验证码
    static func sendPassword(phoneNumber:String, vcode:String, password:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.sendPassword(phoneNumber:phoneNumber, vcode:vcode, password: password)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                if data["isCreate"].bool == true{
                    callback(true)
                }else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
                print("连接失败")
            }
        }
    }
    
    /// 登录
    static func login(phoneNum:String, password:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.login(phoneNum:phoneNum, password:password)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let isLogin = data.count
                if isLogin != 0 {
                    User.getUser().generalPoints = data["generalPoints"].double
                    User.getUser().availablePoints = data["availablePoints"].double
                    User.getUser().id = data["userID"].string
                    print(data["userID"].string)
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
                print("连接失败")
            }
			callback(true)
        }
    }
    /// 获得积分信息
    static func getPointsInfo(callback:@escaping (_ result:Bool)->()){
        if let id = User.getUser().id{
            provider.request(.getPointsInfo(userID: id)){result in
                if case let .success(response) = result {
                    if response.statusCode == 200 {
                        let data = JSON(try? response.mapJSON())
                        User.getUser().availablePoints = data["availablePoints"].double
                        User.getUser().generalPoints = data["generalPoints"].double
                        callback(true)
                    }else{
                        callback(false)
                    }
                    
                }
                if case .failure(_) = result{
                    callback(false)
                    print("连接失败")
                }
                
            }
        }
        else{
            callback(false)
        }
        
    }
    
    /// 修改密码
    static func changePassword(oldPassword:String, newPassword:String, callback:@escaping (_ result:Bool)->()){
        provider.request(.changePassword(old: oldPassword, new: newPassword)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let status = data["status"].bool
                if status != false {
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
                print("连接失败")
            }
        }
    }
    /// 重置密码
    static func resetPassword(phoneNum:String, newPassword:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.resetPassword(phoneNum: phoneNum, newPassword: newPassword)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let status = data["status"].bool
                if status != false {
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
            }
        }
    }
    
    /// 获取重置密码的验证码
    static func getResetVCode(phoneNum:String, vcode:String, callback:@escaping (_ result:Bool)->()){
        provider.request(.getResetVCode(phoneNum:phoneNum, vcode: vcode)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let status = data["status"].bool
                if status != false {
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
            }
        }
    }
    
    //花旗卡相关
    /// 绑定花旗银行卡
    static func bindCard(citiCardNum:String,phoneNum:String,ID:String,password:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.bindCard(citiCardNum:citiCardNum, phoneNum:phoneNum, ID:ID, password:password)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let isBinding = data["isBinding"]
                if isBinding != "false" {
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
                print("连接失败")
            }
        }
    }
    /// 解绑银行卡
    static func unbind(citiCardNum:String,phoneNum:String,ID:String,password:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.unbind(citiCardNum:citiCardNum, phoneNum:phoneNum, ID:ID, password:password)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let isUnBinding = data["unBinding"]
                if isUnBinding != "false" {
                    callback(true)
                }
                else{
                    callback(false)
                }
            }
            if case .failure(_) = result{
                callback(false)
                print("连接失败")
            }
        }
    }
    
    // 商户相关
    /// 获取从start开始的n条商户信息
    static func getMerchantsInfos(start:Int,n:Int,callback:@escaping (_ result:Bool, _ merchants:[(Merchant)])->()){
        provider.request(.getMerchantsInfos(start:start, n:n)){ result in
            if case let .success(response) = result{
                let dataJson = try? response.mapJSON()
                var merchants = [Merchant]()
                if let json = dataJson {
                    let datas = JSON(json).array
                    for data in datas! {
                        let merchant = Merchant()
                        merchant.id = data["merchantID"].string!
                        merchant.name = data["name"].string!
                        merchant.description = data["description"].string!
                        merchant.logoURL = data["logoURL"].string!
                        merchant.address = data["address"].string!
                        merchants.append(merchant)
                    }
                    callback(true,merchants)
                }else{
                    callback(false,merchants)
                }
            }
            if case .failure(_) = result{
                callback(false,[Merchant]())
                print("连接失败")
            }
        }
    }
    /// 根据id获得商户信息
    static func getMerchantInfoByID(id:String,callback:@escaping (_ result:Bool, _ mechant:Merchant)->()){
        provider.request(.getMerchantInfoByID(id: id)){ result in
            if case let .success(response) = result{
                let dataJSON = try? response.mapJSON()
                let merchant = Merchant()
                if let json = dataJSON {
                    let data = JSON(json)
                    merchant.id = data["merchantID"].string!
                    merchant.name = data["name"].string!
                    merchant.description = data["description"].string!
                    merchant.logoURL = data["logoURL"].string!
                    callback(true,merchant)
                }else{
                    callback(false,merchant)
                }
            }
            if case .failure(_) = result{
                callback(false,Merchant())
                print("连接失败")
            }
        }
    }
    /// 获得商户数量
    static func getMerchantCount(callback:@escaping (_ result:Bool, _ num:Int)->()){
        provider.request(.getMerchantCount()){ result in
            if case let .success(response) = result{
                let dataJSON = try? response.mapJSON()
                if let json = dataJSON {
                    let data = JSON(json)
                    let num = data["num"].int
                    callback(true,num!)
                }else{
                    callback(false, 0)
                }
            }
            if case .failure(_) = result{
                callback(false, 0)
                print("连接失败")
            }
        }
    }
    
    // 会员卡相关
    /// 获取指定用户积分最多的n张卡
    static func getMostPointCards(n:Int, callback:@escaping (_ result:Bool, _ cards:[Card])->()){
        provider.request(.getMostPointCards(n:n)){ result in
            if case let .success(response) = result{
                let dataJSON = try? response.mapJSON()
                var cards = [Card]()
                if let json = dataJSON {
                    let datas = JSON(json).array
                    
                    for data in datas! {
                        let card = Card()
                        let merchant = Merchant()
                        merchant.id = data["merchantID"].string!
                        merchant.logoURL = data["merchantLogoURL"].string
                        merchant.name = data["merchantName"].string!
                        card.merchant = merchant
                        card.points = data["points"].double!
                        card.proportion = data["proportion"].double
                        card.logoURL = data["logoURL"].string
                        cards.append(card)
                    }
                    callback(true,cards)
                }else{
                    callback(false,cards)
                }
            }
            if case .failure(_) = result{
                callback(false,[Card]())
                print("连接失败")
            }
            
        }
    }
    
    /// 绑定会员卡
    static func addCard(merchantID:String, cardID:String, cardNum:String, password:String, callback:@escaping (_ result:Bool)->()){
        provider.request(.addCard(merchantID: merchantID, cardNum: cardNum, password: password)){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                if data["status"].bool == true{
                    callback(true)
                }else{
                    callback(false)
                }

            }

        }

    }

    /// 获取用户卡数量
    static func getCardCount(callback:@escaping (_ result:Bool, _ num:Int)->()){
        provider.request(.getCardCount()){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let num = data["num"].int
                if let count = num {
                    callback(true,count)
                }
                else{
                    callback(false,0)
                }
            }
            if case .failure(_) = result{
                callback(false,0)
            }
            
        }
    }

    /// 获取卡详情
    static func getCardDetail(merchantID:String, callback:@escaping (_ result:Bool, _ card:Card)->()){
        provider.request(.getCardDetail(merchantID: merchantID)){ result in
            let card = Card()
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                card.logoURL = data["cardLogoURL"].string
                card.points = data["points"].double!
                card.number = data["cardNum"].string
                card.description = data["cardDescription"].string
                card.type = data["type"].int
                callback(true,card)
            }
            if case .failure(_) = result{
                callback(false,card)
            }
            
        }
    }

    // 积分相关
    /// 积分兑换
    static func changePoints(merchants:[JSON],callback:@escaping (_ result:Bool,_ failureMerchant:Dictionary<String,String>)->()){
        provider.request(.changePoints(merchants: merchants)){ result in
            
            if case let .success(response) = result{
                let dataJson = try? response.mapJSON()
                var failureMerchants = Dictionary<String,String>()
                if let json = dataJson {
                    let datas = JSON(json).array
                    for data in datas! {
                        let merchantID = data["merchantID"].string
                        let reason = data["reason"].string
                        failureMerchants[merchantID!] = reason
                    }
            }
            if case .failure(_) = result{
                
            }
            
        }
    }

}

    /// 获取通用积分
    static func getGeneralPoints(callback:@escaping (_ result:Bool, _ generalPoint:Double)->()){
        provider.request(.getGeneralPoints()){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let generalPoint = data["generalPoints"].double
                if let point = generalPoint{
                    callback(true,point)
                }else{
                    callback(false,0.0)
                }
            }
            if case .failure(_) = result {
                callback(false,0.0)
            }
        }
    }
    
    /// 获取可兑换积分
    static func getAvailablePoints(callback:@escaping (_ result:Bool, _ generalPoint:Double)->()){
        provider.request(.getAvailablePoints()){ result in
            if case let .success(response) = result{
                let data = JSON(try? response.mapJSON())
                let generalPoint = data["availablePoints"].double
                if let point = generalPoint{
                    callback(true,point)
                }else{
                    callback(false,0.0)
                }
            }
            if case .failure(_) = result {
                callback(false,0.0)
            }
        }
    }

    /// 获取一个人所有积分兑换记录
    static func getAllPointsHistory(callback:@escaping (_ result:Bool,_ pointsHistory:[PointsHistory])->()){
        provider.request(.getAllPointsHistory()){ result in
            var pointsHistory = [PointsHistory]()
            if case let .success(response) = result{
                let decoder = JSONDecoder()
                do{
                    pointsHistory = try decoder.decode([PointsHistory].self, from: response.data)
                    callback(true,pointsHistory)
                }catch{
                    callback(false,pointsHistory)
                }
                
            }
            if case .failure(_) = result {
                callback(false,pointsHistory)
            }
        }
    }
    
    /// 获取一个人某张卡的积分兑换记录
    static func getPointsHistoryByMerchantID(merchantID:String, callback:@escaping (_ result:Bool,_ pointsHistory:PointsHistory)->()){
        provider.request(.getPointsHistoryByMerchantID(merchantID:merchantID)){ result in
            var pointsHistory = PointsHistory()
            if case let .success(response) = result{
                let decoder = JSONDecoder()
                do{
                    pointsHistory = try decoder.decode(PointsHistory.self, from: response.data)
                }catch{
                    callback(false,pointsHistory)
                }
                
                
            }
            if case .failure(_) = result {
                callback(false,pointsHistory)
            }
        }
    }
    
    
    // 支付相关
    /// TODO: 二维码轮询
    static func pollizngQR(timestamp:String,callback:@escaping (_ result:Bool)->()){
        provider.request(.pollingQR(timestamp: timestamp)){ result in
            if case let .success(response) = result{
                
            }
        }
    }
}
