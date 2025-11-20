package com.thanesgroup.lgs.data.model

data class DispenseModel(
  val hn: String,
  val patientName: String,
  val orders: List<OrderModel>
)

data class CheckDrugModel(
  val isNarcotic: Boolean
)

data class ReceiveOrderModel(
  val message: Boolean
)

data class LabelModel(
  val f_orderitemname: String,
  val f_orderqty: String,
  val f_orderunitdesc: String,
  val f_itemlocationno: String,
  val f_referenceCode: String
)

data class OrderModel(
  val f_orderitemcode: String,
  val f_orderitemname: String,
  val f_prescriptionnohis: String,
  val f_orderqty: String,
  val f_orderunitdesc: String,
  val f_itemlocationno: String,
  val f_referenceCode: String,
  val f_status: String,
  val f_dispensestatus: String,
  val f_patientname: String
)

data class DispenseOnModel(
  val drugCode: String,
  val drugName: String,
  val location: String
)

data class ManualDispenseReturnModel(
  val statusCode: Int,
  val message: String,
  val data: DispenseOnModel?
)