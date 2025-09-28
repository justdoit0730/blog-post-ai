package org.justdoit.blog.service.s3;

import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.ai.write.AiWrite;
import org.justdoit.blog.entity.ai.write.AiWriteRepository;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.util.*;

import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final GlobalVariables globalVariables;
    private final AiWriteRepository aiWriteRepository;

    public List<String> uploadImages(SessionUser sessionUser, List<String> base64Images) {
        S3Client s3Client = globalVariables.s3Client;
        S3Presigner s3Presigner = globalVariables.s3Presigner;
        String bucketName = globalVariables.S3_BUCKET_NAME;
        String email = sessionUser.getEmail();
        List<String> presSignedUrls = new ArrayList<>();

        for (String base64Image : base64Images) {
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            base64Image = base64Image.replaceAll("\\s", "");
            try {
                byte[] imageBytes = decodeBase64(base64Image);
                String key = String.format("images/%s/%d.jpg", email, System.currentTimeMillis());

                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.putObject(putReq, RequestBody.fromBytes(imageBytes));

//                String url = s3Presigner.presignGetObject(presignRequest(bucketName, key)).url().toString();
                String url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, key);
                presSignedUrls.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sessionUser.setWriteImgUrls(presSignedUrls);
        return presSignedUrls;
    }

    private GetObjectPresignRequest presignRequest(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .build();
    }

    // 불필요 이미지 삭제
    public void cleanS3Images(SessionUser sessionUser, List<String> imgNames) {
        String email = sessionUser.getEmail();
        String prefix = "images/" + email + "/";

        S3Client s3Client = globalVariables.s3Client;
        String bucketName = globalVariables.S3_BUCKET_NAME;

        // 1️⃣ S3에 있는 모든 파일 조회
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        List<String> s3Files = listRes.contents().stream()
                .map(S3Object::key)
                .map(k -> k.substring(prefix.length())) // 파일명만
                .toList();

        // 2️⃣ DB에서 해당 이메일의 모든 글 가져오기
        List<AiWrite> aiWrites = aiWriteRepository.findByCafeUserEmail(email);

        // 3️⃣ DB에 있는 이미지 파일 목록 통합
        Set<String> dbImgList = new HashSet<>();
        for (AiWrite aiWrite : aiWrites) {
            if (aiWrite.getImgList() != null && !aiWrite.getImgList().isBlank()) {
                Arrays.stream(aiWrite.getImgList().split(","))
                        .map(String::trim)
                        .forEach(dbImgList::add);
            }
        }

        // 4️⃣ 보존할 파일 = imgNames + DB img_list
        Set<String> keepFiles = new HashSet<>(imgNames);
        keepFiles.addAll(dbImgList);

        // 5️⃣ 삭제 대상 = S3에 있지만 보존 파일에 없는 것
        List<String> toDelete = s3Files.stream()
                .filter(f -> !keepFiles.contains(f))
                .toList();

        if (!toDelete.isEmpty()) {
            List<ObjectIdentifier> objectsToDelete = toDelete.stream()
                    .map(name -> ObjectIdentifier.builder().key(prefix + name).build())
                    .toList();

            DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            s3Client.deleteObjects(deleteReq);

            System.out.println("삭제 완료 파일 수: " + objectsToDelete.size());
        } else {
            System.out.println("삭제할 파일 없음");
        }
    }

//    public void deleteImages(SessionUser sessionUser, List<String> fileNames) {
//        S3Client s3Client = globalVariables.s3Client;
//        S3Presigner s3Presigner = globalVariables.s3Presigner;
//        String bucketName = globalVariables.S3_BUCKET_NAME;
//        String email = sessionUser.getEmail();
//        List<ObjectIdentifier> objectsToDelete = fileNames.stream()
//                .map(name -> ObjectIdentifier.builder().key(String.format("images/%s/%s", email, name)).build())
//                .toList();
//
//        DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
//                .bucket(bucketName)
//                .delete(Delete.builder().objects(objectsToDelete).build())
//                .build();
//
//        s3Client.deleteObjects(deleteReq);
//
//        System.out.println("총 " + objectsToDelete.size() + "개 파일 삭제 완료");
//    }

    // 게시글 삭제 시 S3 에 저장된 사진도 삭제
    public void deleteImages(SessionUser sessionUser, List<String> fileNames) {
        S3Client s3Client = globalVariables.s3Client;
        String bucketName = globalVariables.S3_BUCKET_NAME;
        String email = sessionUser.getEmail();

        List<ObjectIdentifier> objectsToDelete = fileNames.stream()
                .map(name -> ObjectIdentifier.builder()
                        .key(String.format("images/%s/%s", email, name))
                        .build())
                .toList();

        DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(objectsToDelete).build())
                .build();

        s3Client.deleteObjects(deleteReq);
        System.out.println("총 " + objectsToDelete.size() + "개 파일 삭제 완료");
    }

    // 계정 탈퇴 시 이메일 디렉터리 삭제
    public void deleteEmailDirectory(SessionUser sessionUser) {
        S3Client s3Client = globalVariables.s3Client;
        S3Presigner s3Presigner = globalVariables.s3Presigner;
        String bucketName = globalVariables.S3_BUCKET_NAME;

        String email = sessionUser.getEmail();
        String prefix = "images/" + email + "/";

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        if (listRes.contents().isEmpty()) {
            System.out.println("삭제할 파일 없음: " + prefix);
            return;
        }

        List<ObjectIdentifier> toDelete = listRes.contents().stream()
                .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                .toList();

        DeleteObjectsRequest delReq = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(toDelete).build())
                .build();
        s3Client.deleteObjects(delReq);
        System.out.println("디렉터리 '" + prefix + "' 내 파일 " + toDelete.size() + "개 삭제 완료");
    }

}
