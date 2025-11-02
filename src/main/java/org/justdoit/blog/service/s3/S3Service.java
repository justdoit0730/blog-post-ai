package org.justdoit.blog.service.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.write.AiWriteSaveDto;
import org.justdoit.blog.dto.post.PostAiDto;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.*;

import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final GlobalVariables globalVariables;

    public void moveImagesFromWriteCacheToWrite(AiWriteSaveDto aiWriteSaveDto) {
        S3Client s3 = globalVariables.s3Client;
        String bucket = globalVariables.S3_BUCKET_NAME;

        List<String> imgUrls = aiWriteSaveDto.getImgUrlS();

        for (String imgUrl : imgUrls) {
            String sourceKey = imgUrl.split("amazonaws.com/")[1]; // images/.../writeCache/xxx.jpg
            String targetKey = sourceKey.replace("writeCache/", "write/");

            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build();
            s3.copyObject(copyReq);
        }

        String updatedContent = aiWriteSaveDto.getFullContent().replaceAll(
                "(https://[^\"]+/images/[^\"']+/)writeCache/",
                "$1write/"
        );
        aiWriteSaveDto.setFullContent(updatedContent);
    }

    public void moveImagesFromWriteCacheToWrite(PostAiDto postAiDto) throws JsonProcessingException {
        S3Client s3 = globalVariables.s3Client;
        String bucket = globalVariables.S3_BUCKET_NAME;
        ObjectMapper mapper = new ObjectMapper();

        List<String> imgUrls = mapper.readValue(postAiDto.getImgUrls(), List.class);

        for (String imgUrl : imgUrls) {
            String sourceKey = imgUrl.split("amazonaws.com/")[1]; // images/.../writeCache/xxx.jpg
            String targetKey = sourceKey.replace("aiGenerationPostCache/", "aiPost/");

            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build();
            s3.copyObject(copyReq);
        }

        String updatedContent = postAiDto.getContentHtml().replaceAll(
                "(https://[^\"]+/images/[^\"']+/)aiGenerationPostCache/",
                "$1aiPost/"
        );
        postAiDto.setContentHtml(updatedContent);
    }

    public void moveImagesFromPostCacheToWrite(AiWriteSaveDto aiWriteSaveDto) {
        S3Client s3 = globalVariables.s3Client;
        String bucket = globalVariables.S3_BUCKET_NAME;

        List<String> imgUrls = aiWriteSaveDto.getImgUrlS();

        for (String imgUrl : imgUrls) {
            String sourceKey = imgUrl.split("amazonaws.com/")[1]; // images/.../writeCache/xxx.jpg
            String targetKey = sourceKey.replace("aiGenerationPostCache/", "aiPost/");

            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .build();
            s3.copyObject(copyReq);
        }

        String updatedContent = aiWriteSaveDto.getFullContent().replaceAll(
                "(https://[^\"]+/images/[^\"']+/)aiGenerationPostCache/",
                "aiPost/"
        );
        aiWriteSaveDto.setFullContent(updatedContent);
    }

    public List<String> uploadImages(SessionUser sessionUser, List<String> base64Images, String format) {
        System.out.println(format);
        S3Client s3Client = globalVariables.s3Client;
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
                String key = String.format("images/%s/%s/%d.jpg", email, format, System.currentTimeMillis());

                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.putObject(putReq, RequestBody.fromBytes(imageBytes));

                String url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, key);
                presSignedUrls.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sessionUser.setWriteImgUrls(presSignedUrls);
        return presSignedUrls;
    }

    public void cleanS3CacheImage(SessionUser sessionUser, String format) {
        String email = sessionUser.getEmail();
        String prefix = String.format("images/%s/%s/", email, format);

        S3Client s3Client = globalVariables.s3Client;
        String bucketName = globalVariables.S3_BUCKET_NAME;

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        List<String> s3Files = listRes.contents().stream()
                .map(S3Object::key)
                .toList();

        if (!s3Files.isEmpty()) {
            List<ObjectIdentifier> objectsToDelete = s3Files.stream()
                    .map(name -> ObjectIdentifier.builder().key(name).build())
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

    // 게시글 삭제 시 S3 에 저장된 사진도 삭제
    public void deleteImages(SessionUser sessionUser, List<String> fileNames, String format) {
        S3Client s3Client = globalVariables.s3Client;
        String bucketName = globalVariables.S3_BUCKET_NAME;
        String email = sessionUser.getEmail();

        List<ObjectIdentifier> objectsToDelete = fileNames.stream()
                .map(name -> ObjectIdentifier.builder()
                        .key(String.format("images/%s/%s/%s", email, format, name))
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