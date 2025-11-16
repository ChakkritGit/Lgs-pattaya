package com.thanesgroup.lgs.data.model

data class UpdateInfo(
  val id: Int,
  val version_code: String,
  val version_name: String,
  val apk_url: String,
  val changelog: String,
  val created_at: String,
  val updated_at: String
)