package com.swe7.aym.jpa.post;

import com.swe7.aym.jpa.category.Category;
import com.swe7.aym.jpa.category.CategoryRepository;
import com.swe7.aym.jpa.member.Member;
import com.swe7.aym.jpa.member.MembersService;
import com.swe7.aym.jpa.member.dto.MemberDto;
import com.swe7.aym.jpa.post.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final MembersService membersService;

    public Long save(PostSaveDto requestDto) {
        MemberDto client = membersService.findByEmail(requestDto.getClient_email());
        MemberDto helper = membersService.findByEmail(requestDto.getClient_email());

        Post res = Post.builder()
                .client(client.toEntity())
                .helper(helper.toEntity())
                .product(requestDto.getProduct())
                .contents(requestDto.getContents())
                .destination(requestDto.getDestination())
                .category(categoryRepository.findByCategoryId(requestDto.getCategory()))
                .client_star(0)
                .helper_star(0)
                .fee(requestDto.getFee())
                .cost(requestDto.getCost())
                .createTime(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .state(0)
                .build();
        return postRepository.save(res).getPostId();
    }

    public Long updateEnd(Long id, PostEndDto postEndDto, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다!"));
        System.out.println(email);
        System.out.println(post.getClient().getEmail());
        System.out.println(post.getHelper().getEmail());
        if (post.getClient().getEmail().equals(email)){
            post.updateEnd("client", Integer.parseInt(postEndDto.getStar()));
        }
        if (post.getHelper().getEmail().equals(email)){
            post.updateEnd("helper", Integer.parseInt(postEndDto.getStar()));
        }
        return id;
    }

    public Long updateHelper(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다!"));
        Member helper = membersService.findByEmail(email).toEntity();
        post.updateHelper(helper);
        return id;
    }

    public PostResponseDto findById(Long target_id) {
        Post entity = postRepository.findById(target_id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 조회 : 잘못된 아이디"));
        return new PostResponseDto(entity);
    }

    public List<PostDto> findByState(int target_state) {
        return postRepository.findByState(target_state)
                .stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    public List<PostSimpleDto> findByRecentWithEmail(String email) {
        int state = 0; // 등록되서 매칭안된 것만
        MemberDto client = membersService.findByEmail(email);
        List<PostSimpleDto> res = postRepository.findByClientAndState(client.toEntity(), state)
                .stream()
                .map(PostSimpleDto::new)
                .collect(Collectors.toList());

        res.addAll(postRepository.findByStateOrderByCreateTime(state)
                .stream()
                .map(PostSimpleDto::new)
                .collect(Collectors.toList()));
        return res;
    }

    public List<PostDto> findByKeyword(String target_keyword) {
        return postRepository.findByContentsContaining(target_keyword)
                .stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    public List<PostDto> findByCategory(String category) {
        Category res1 = categoryRepository.findByContextContaining(category);
        return postRepository.findByCategory(res1)
                .stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    public Long updateCancel(Long id) {
        Post post = postRepository.findById(id).get();
        post.updateCancel();
        return post.getPostId();
    }

    public List<PostHistDto> findByEmail(String email) {
        Member member =  membersService.findByEmail(email).toEntity();
        return postRepository.findByClientAndHelper(member, member).stream()
                .map(PostHistDto::new)
                .collect(Collectors.toList());
    }

    public List<PostSimpleDto> findByEmailAndCancelled(String email) {
        Member member =  membersService.findByEmail(email).toEntity();
        List<PostSimpleDto> res = postRepository.findByClientAndState(member, 3)
                .stream()
                .map(PostSimpleDto::new)
                .collect(Collectors.toList());
        res.addAll(postRepository.findByHelperAndState(member, 3)
                .stream()
                .map(PostSimpleDto::new)
                .collect(Collectors.toList()));
        return res;
    }
}
