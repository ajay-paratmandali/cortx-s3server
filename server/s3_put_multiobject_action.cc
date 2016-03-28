/*
 * COPYRIGHT 2016 SEAGATE LLC
 *
 * THIS DRAWING/DOCUMENT, ITS SPECIFICATIONS, AND THE DATA CONTAINED
 * HEREIN, ARE THE EXCLUSIVE PROPERTY OF SEAGATE TECHNOLOGY
 * LIMITED, ISSUED IN STRICT CONFIDENCE AND SHALL NOT, WITHOUT
 * THE PRIOR WRITTEN PERMISSION OF SEAGATE TECHNOLOGY LIMITED,
 * BE REPRODUCED, COPIED, OR DISCLOSED TO A THIRD PARTY, OR
 * USED FOR ANY PURPOSE WHATSOEVER, OR STORED IN A RETRIEVAL SYSTEM
 * EXCEPT AS ALLOWED BY THE TERMS OF SEAGATE LICENSES AND AGREEMENTS.
 *
 * YOU SHOULD HAVE RECEIVED A COPY OF SEAGATE'S LICENSE ALONG WITH
 * THIS RELEASE. IF NOT PLEASE CONTACT A SEAGATE REPRESENTATIVE
 * http://www.seagate.com/contact
 *
 * Original author:  Kaustubh Deorukhkar   <kaustubh.deorukhkar@seagate.com>
 * Original author:  Rajesh Nambiar        <rajesh.nambiar@seagate.com>
 * Original creation date: 22-Jan-2016
 */

#include "s3_put_multiobject_action.h"
#include "s3_clovis_config.h"
#include "s3_error_codes.h"
#include "s3_perf_logger.h"
#include "s3_log.h"

S3PutMultiObjectAction::S3PutMultiObjectAction(std::shared_ptr<S3RequestObject> req) : S3Action(req), total_data_to_stream(0) {
  s3_log(S3_LOG_DEBUG, "Constructor\n");
  part_number = get_part_number();
  upload_id = request->get_query_string_value("uploadId");
  setup_steps();
}

void S3PutMultiObjectAction::setup_steps(){
  s3_log(S3_LOG_DEBUG, "Setting up the action\n");

  add_task(std::bind( &S3PutMultiObjectAction::fetch_bucket_info, this ));
  add_task(std::bind( &S3PutMultiObjectAction::fetch_multipart_metadata, this ));
  if(part_number != 1) {
    add_task(std::bind( &S3PutMultiObjectAction::fetch_firstpart_info, this ));
  }
  add_task(std::bind( &S3PutMultiObjectAction::create_object, this ));
  add_task(std::bind( &S3PutMultiObjectAction::initiate_data_streaming, this ));
  add_task(std::bind( &S3PutMultiObjectAction::save_metadata, this ));
  add_task(std::bind( &S3PutMultiObjectAction::send_response_to_s3_client, this ));
  // ...
}

void S3PutMultiObjectAction::fetch_bucket_info() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  if (!request->get_buffered_input().is_freezed()) {
    request->pause();  // Pause reading till we are ready to consume data.
  }
  bucket_metadata = std::make_shared<S3BucketMetadata>(request);
  bucket_metadata->load(std::bind( &S3PutMultiObjectAction::next, this), std::bind( &S3PutMultiObjectAction::next, this));
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::fetch_multipart_metadata() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  object_multipart_metadata = std::make_shared<S3ObjectMetadata>(request, true, upload_id);
  object_multipart_metadata->load(std::bind( &S3PutMultiObjectAction::next, this), std::bind( &S3PutMultiObjectAction::fetch_multipart_failed, this));
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::fetch_multipart_failed() {
  //Log error
  s3_log(S3_LOG_ERROR, "Failed to retrieve multipart metadata\n");
  request->resume();
  send_response_to_s3_client();
}

void S3PutMultiObjectAction::fetch_firstpart_info() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  if (!request->get_buffered_input().is_freezed()) {
    request->pause();  // Pause reading till we are ready to consume data.
  }
  part_metadata = std::make_shared<S3PartMetadata>(request, upload_id, part_number);
  part_metadata->load(std::bind( &S3PutMultiObjectAction::next, this), std::bind( &S3PutMultiObjectAction::fetch_firstpart_info_failed, this), 1);
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::fetch_firstpart_info_failed() {
  s3_log(S3_LOG_WARN, "Failed to retrieve first part metadata\n");
  request->resume();
  send_response_to_s3_client();
}


void S3PutMultiObjectAction::create_object() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  size_t offset = 0;
  create_object_timer.start();
  if (bucket_metadata->get_state() == S3BucketMetadataState::present) {
    if(part_number > 1) {
      size_t part_one_size = part_metadata->get_content_length();
      if(part_metadata->get_state() == S3PartMetadataState::present) {
        s3_log(S3_LOG_DEBUG, "part size = %zu\n", request->get_content_length());
        // Calculate offset
        offset = (part_number - 1) * part_one_size;
        s3_log(S3_LOG_DEBUG, "offset for clovis write = %zu\n", offset);
      } else {
          // Send error message TODO
          s3_log(S3_LOG_DEBUG, "Part 1 metadata doesn't exist\n");
          send_response_to_s3_client();
        }
    }
    clovis_writer = std::make_shared<S3ClovisWriter>(request, offset);
    if(part_number == 1) {
      // Create object once, for first part
      clovis_writer->create_object(std::bind( &S3PutMultiObjectAction::next, this), std::bind( &S3PutMultiObjectAction::create_object_failed, this));
    } else {
      next();
    }
  } else {
    request->resume();
    send_response_to_s3_client();
  }
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::create_object_failed() {
  // TODO - do anything more for failure?
  s3_log(S3_LOG_DEBUG, "Entering\n");
  if (clovis_writer->get_state() == S3ClovisWriterOpState::exists) {
    // If object exists, S3 overwrites it.
    s3_log(S3_LOG_DEBUG, "Existing object: Overwrite it.\n");
    next();
  } else {
    create_object_timer.stop();
    LOG_PERF("create_object_failed_ms", create_object_timer.elapsed_time_in_millisec());
    s3_log(S3_LOG_ERROR, "Create object failed\n");
    request->resume();
    // Any other error report failure.
    send_response_to_s3_client();
  }
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::initiate_data_streaming() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  create_object_timer.stop();
  LOG_PERF("create_object_successful_ms", create_object_timer.elapsed_time_in_millisec());

  total_data_to_stream = request->get_content_length();
  request->resume();

  if (total_data_to_stream == 0) {
    save_metadata();  // Zero size object.
  } else {
    if (request->has_all_body_content()) {
      write_object(request->get_buffered_input());
    } else {
      s3_log(S3_LOG_DEBUG, "We do not have all the data, so start listening....\n");
      // Start streaming, logically pausing action till we get data.
      request->listen_for_incoming_data(
          std::bind(&S3PutMultiObjectAction::consume_incoming_content, this),
          S3ClovisConfig::get_instance()->get_clovis_write_payload_size()
        );
    }
  }
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::consume_incoming_content() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  // Resuming the action since we have data.
  write_object(request->get_buffered_input());
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::write_object(S3AsyncBufferContainer& buffer) {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  if (buffer.is_freezed()) {
    // This is last one, no more data ahead.
    s3_log(S3_LOG_DEBUG, "This is last one, no more data ahead, write it.\n");
    clovis_writer->write_content(std::bind( &S3PutMultiObjectAction::write_object_successful, this), std::bind( &S3PutMultiObjectAction::write_object_failed, this), buffer);
  } else {
    request->pause();  // Pause till write to clovis is complete
    s3_log(S3_LOG_DEBUG, "We will still be expecting more data, so write it and pause to wait for more data\n");
    // We will still be expecting more data, so write and pause to wait for more data
    clovis_writer->write_content(std::bind( &S3RequestObject::resume, request), std::bind( &S3PutMultiObjectAction::write_object_failed, this), buffer);
  }
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::write_object_successful() {
  s3_log(S3_LOG_DEBUG, "Write successful\n");
  if (request->get_buffered_input().length() > 0) {
    // We still have more data to write.
    write_object(request->get_buffered_input());
  } else {
    next();
  }
}

void S3PutMultiObjectAction::write_object_failed() {
  s3_log(S3_LOG_ERROR, "Write to clovis failed\n");
  send_response_to_s3_client();
}

void S3PutMultiObjectAction::save_metadata() {
  s3_log(S3_LOG_DEBUG, "Entering\n");
  part_metadata = std::make_shared<S3PartMetadata>(request, upload_id, part_number);
  part_metadata->set_content_length(request->get_header_value("Content-Length"));
  part_metadata->set_md5(clovis_writer->get_content_md5());
  for (auto it: request->get_in_headers_copy()) {
    if(it.first.find("x-amz-meta-") != std::string::npos) {
      part_metadata->add_user_defined_attribute(it.first, it.second);
    }
  }
  part_metadata->save(std::bind( &S3PutMultiObjectAction::next, this), std::bind( &S3PutMultiObjectAction::next, this));
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}

void S3PutMultiObjectAction::send_response_to_s3_client() {
  s3_log(S3_LOG_DEBUG, "Entering\n");

  if (bucket_metadata->get_state() == S3BucketMetadataState::missing) {
    s3_log(S3_LOG_ERROR, "Missing bucket for multipart upload, upload id = %s, request id = %s object uri = %s\n",upload_id.c_str(), request->get_request_id().c_str(), request->get_object_uri().c_str());
    // Invalid Bucket Name
    S3Error error("NoSuchBucket", request->get_request_id(), request->get_object_uri());
    std::string& response_xml = error.to_xml();
    request->set_out_header_value("Content-Type", "application/xml");
    request->set_out_header_value("Content-Length", std::to_string(response_xml.length()));

    request->send_response(error.get_http_status_code(), response_xml);
  } else if (object_multipart_metadata && (object_multipart_metadata->get_state() == S3ObjectMetadataState::missing)) {
    // The multipart upload may have been aborted
    s3_log(S3_LOG_WARN, "The metadata of multipart upload doesn't exist, upload id = %s request id = %s object uri = %s\n",
           upload_id.c_str(), request->get_request_id().c_str(), request->get_object_uri().c_str());
    S3Error error("NoSuchUpload", request->get_request_id(), request->get_object_uri());
    std::string& response_xml = error.to_xml();
    request->set_out_header_value("Content-Type", "application/xml");
    request->set_out_header_value("Content-Length", std::to_string(response_xml.length()));

    request->send_response(error.get_http_status_code(), response_xml);
  } else if (part_metadata && (part_metadata->get_state() == S3PartMetadataState::missing)) {
    // May happen if part 2/3... comes before part 1, in that case those part
    // upload need to be retried(by that time part 1 meta data will get in)
    s3_log(S3_LOG_WARN, "Part one metadata is not available, asking client to retry, upload id = %s request id = %s object uri = %s\n",
         upload_id.c_str(),  request->get_request_id().c_str(), request->get_object_uri().c_str());
    S3Error error("ServiceUnavailable", request->get_request_id(), request->get_object_uri());
    std::string& response_xml = error.to_xml();
    request->set_out_header_value("Content-Type", "application/xml");
    request->set_out_header_value("Content-Length", std::to_string(response_xml.length()));
    // Let the client retry after 1 second delay
    request->set_out_header_value("Retry-After", "1");
    request->send_response(error.get_http_status_code(), response_xml);

  } else if (clovis_writer->get_state() == S3ClovisWriterOpState::failed) {
    s3_log(S3_LOG_ERROR, "Clovis failed to write for multipart upload, upload id = %s request id = %s object uri = %s",
           upload_id.c_str(), request->get_request_id().c_str(), request->get_object_uri().c_str());
    S3Error error("InternalError", request->get_request_id(), request->get_object_uri());
    std::string& response_xml = error.to_xml();
    request->set_out_header_value("Content-Type", "application/xml");
    request->set_out_header_value("Content-Length", std::to_string(response_xml.length()));

    request->send_response(error.get_http_status_code(), response_xml);
  } else if (part_metadata->get_state() == S3PartMetadataState::saved) {
    request->set_out_header_value("ETag", clovis_writer->get_content_md5());

    request->send_response(S3HttpSuccess200);
  } else {
    s3_log(S3_LOG_ERROR, "Internal error upload id = %s request id = %s object uri = %s\n",
           upload_id.c_str(), request->get_request_id().c_str(), request->get_object_uri().c_str());
    S3Error error("InternalError", request->get_request_id(), request->get_object_uri());
    std::string& response_xml = error.to_xml();
    request->set_out_header_value("Content-Type", "application/xml");
    request->set_out_header_value("Content-Length", std::to_string(response_xml.length()));

    request->send_response(error.get_http_status_code(), response_xml);
  }
  request->resume();

  done();
  i_am_done();  // self delete
  s3_log(S3_LOG_DEBUG, "Exiting\n");
}
