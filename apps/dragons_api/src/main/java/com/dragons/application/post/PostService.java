package com.dragons.application.post;

import com.dragons.application.post.dto.PostDeleteCommand;
import com.dragons.application.post.dto.PostDeleteResult;
import com.dragons.application.post.dto.PostGetCommand;
import com.dragons.application.post.dto.PostGetResult;
import com.dragons.application.post.dto.PostSearchCondition;
import com.dragons.application.post.dto.PostSearchResult;
import com.dragons.application.post.dto.PostUpdateCommand;
import com.dragons.application.post.dto.PostUpdateResult;
import com.dragons.application.post.dto.PostWriteCommand;
import com.dragons.application.post.dto.PostWriteResult;
import com.dragons.config.jwt.JwtTokenProvider;
import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;
  private final JwtTokenProvider jwtTokenProvider;

  // 작성
  public PostWriteResult write(PostWriteCommand command) {
    String author = jwtTokenProvider.getPayload(command.token());

    Post post = postRepository.save(
        Post.write(
            command.title(),
            command.content(),
            command.category(),
            command.isPublic(),
            author));

    return new PostWriteResult(
        post.getId(),
        post.title(),
        command.content(),
        command.category(),
        command.isPublic(),
        author);
  }

  // 목록 조회
  public PostSearchResult search(PostSearchCondition condition) {
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    if (condition.sort() != null && !condition.sort().isBlank()) {
      String[] split = condition.sort().split(",");
      if (split.length == 2) {
        try {
          sort = Sort.by(Sort.Direction.fromString(split[1].trim()), split[0].trim());
        } catch (IllegalArgumentException e) {
          // 기본 정렬 유지
        }
      }
    }

    Pageable pageable = PageRequest.of(condition.page() - 1, condition.limit(), sort);
    Page<Post> result = postRepository.findAllByDeletedAtIsNull(pageable);

    return new PostSearchResult(
        result.getContent().stream()
            .map(post -> new PostSearchResult.PostSummary(
                post.getId(),
                post.title(),
                post.category(),
                post.author()))
            .toList(),
        result.getTotalElements(),
        result.getNumber() + 1,
        result.getSize());
  }

  // 조회
  public PostGetResult get(PostGetCommand command) {
    Long postId = command.postId();
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new CoreException(ErrorType.NOT_FOUND, "게시글이 존재하지 않습니다."));
    return new PostGetResult(
        post.getId(),
        post.title(),
        post.content(),
        post.category(),
        post.author());
  }

  // 수정
  @Transactional
  public PostUpdateResult update(PostUpdateCommand command) {
    Long postId = command.postId();
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new CoreException(ErrorType.NOT_FOUND, "게시글이 존재하지 않습니다."));

    String author = jwtTokenProvider.getPayload(command.token());
    post.checkAuthor(author);
    post.reWrite(command.title(), command.content());

    return new PostUpdateResult(
        post.getId(),
        post.title(),
        post.author());
  }

  // 삭제
  @Transactional
  public PostDeleteResult delete(PostDeleteCommand command) {
    Long postId = command.postId();
    Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
        () -> new CoreException(ErrorType.NOT_FOUND, "게시글이 존재하지 않습니다."));
    String author = jwtTokenProvider.getPayload(command.token());
    post.checkAuthor(author);
    post.delete();

    return new PostDeleteResult(
        post.getId(),
        post.title(),
        post.author());
  }
}
