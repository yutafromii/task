// src/main/java/com/example/app/dto/PostCommentResponse.java
package com.example.ecapp.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentResponse {

    private Long id;
    private String content;
    private Long userId;
    private String userName;  // 任意：ユーザー名を表示するため
    private LocalDateTime createdAt;

    private List<PostCommentResponse> replies = new ArrayList<>(); // 子コメント
}
