package app.ecosynergy.api.services;

import app.ecosynergy.api.controllers.TeamController;
import app.ecosynergy.api.data.vo.v1.TeamVO;
import app.ecosynergy.api.exceptions.RequiredObjectIsNullException;
import app.ecosynergy.api.exceptions.ResourceAlreadyExistsException;
import app.ecosynergy.api.exceptions.ResourceNotFoundException;
import app.ecosynergy.api.mapper.DozerMapper;
import app.ecosynergy.api.models.Team;
import app.ecosynergy.api.models.TeamMember;
import app.ecosynergy.api.models.TeamMemberId;
import app.ecosynergy.api.models.User;
import app.ecosynergy.api.repositories.TeamMemberRepository;
import app.ecosynergy.api.repositories.TeamRepository;
import app.ecosynergy.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private PagedResourcesAssembler<TeamVO> assembler;

    private static final Logger logger = Logger.getLogger(TeamService.class.getName());

    public PagedModel<EntityModel<TeamVO>> findAll(Pageable pageable, ZoneId zoneId) {
        Page<Team> teams = teamRepository.findAllWithMembers(pageable);

        Page<TeamVO> teamVOs = teams.map(team -> {
            TeamVO teamVO = convertToVO(team);
            teamVO.setCreatedAt(teamVO.getCreatedAt().withZoneSameInstant(zoneId));
            teamVO.setUpdatedAt(teamVO.getUpdatedAt().withZoneSameInstant(zoneId));
            return teamVO;
        });

        Link link = linkTo(methodOn(TeamController.class)
                .findAll(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().toString(),
                        zoneId.toString()
                ))
                .withSelfRel();

        return assembler.toModel(teamVOs, link);
    }

    public TeamVO findById(Long id, ZoneId zoneId) {
        if(id == null) throw new RequiredObjectIsNullException();

        Optional<Team> teamOpt = teamRepository.findByIdWithMembers(id);

        if(teamOpt.isEmpty()) throw new ResourceNotFoundException("Team not found with the given ID: " + id);

        Team team = teamOpt.get();

        team.setCreatedAt(team.getCreatedAt().withZoneSameInstant(zoneId));
        team.setUpdatedAt(team.getUpdatedAt().withZoneSameInstant(zoneId));

        return convertToVO(team);
    }

    public TeamVO findByHandle(String teamHandle, ZoneId zoneId) {
        if(teamHandle == null) throw new RequiredObjectIsNullException();
        teamHandle = teamHandle.toLowerCase(Locale.ROOT);

        Optional<Team> teamOpt = teamRepository.findByHandleWithMembers(teamHandle);

        if(teamOpt.isEmpty()) throw new ResourceNotFoundException("Team not found with the given Handle: " + teamHandle);

        Team team = teamOpt.get();

        team.setCreatedAt(team.getCreatedAt().withZoneSameInstant(zoneId));
        team.setUpdatedAt(team.getUpdatedAt().withZoneSameInstant(zoneId));

        return convertToVO(team);
    }

    public List<TeamVO> findByHandleContaining(String teamHandle, ZoneId zoneId) {
        if(teamHandle == null) throw new RequiredObjectIsNullException();

        List<Team> teams = teamRepository.findByHandleContaining(teamHandle.toLowerCase(Locale.ROOT));

        return teams.stream().map(team -> {
            TeamVO teamVO = convertToVO(team);
            teamVO.setCreatedAt(teamVO.getCreatedAt().withZoneSameInstant(zoneId));
            teamVO.setUpdatedAt(teamVO.getUpdatedAt().withZoneSameInstant(zoneId));

            return teamVO;
        }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamVO create(TeamVO team, ZoneId zoneId) {
        if(team == null) throw new RequiredObjectIsNullException();

        team.setHandle(team.getHandle().toLowerCase(Locale.ROOT));

        boolean isHandlerExists = teamRepository.findByHandleWithMembers(team.getHandle()).isPresent();
        if(isHandlerExists) throw new ResourceAlreadyExistsException("A team with the given handle already exists: " + team.getHandle());

        Team teamEntity = teamRepository.save(DozerMapper.parseObject(team, Team.class));

        Set<User> users = team.getMembers().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with the given ID: " + userId)))
                .collect(Collectors.toSet());

        users.forEach(user -> {
            TeamMemberId teamMemberId = new TeamMemberId(teamEntity.getId(), user.getId());

            TeamMember teamMember = new TeamMember();
            teamMember.setId(teamMemberId);
            teamMember.setTeam(teamEntity);
            teamMember.setUser(user);

            teamEntity.getTeamMembers().add(teamMember);
        });

        teamEntity.setCreatedAt(teamEntity.getCreatedAt().withZoneSameInstant(zoneId));
        teamEntity.setUpdatedAt(teamEntity.getUpdatedAt().withZoneSameInstant(zoneId));

        teamRepository.save(teamEntity);

        return convertToVO(teamEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamVO update(Long teamId, TeamVO updatedTeamVO, ZoneId zoneId) {
        if(teamId == null || updatedTeamVO == null) throw new RequiredObjectIsNullException();

        updatedTeamVO.setHandle(updatedTeamVO.getHandle().toLowerCase(Locale.ROOT));

        Team existingTeam = teamRepository.findByIdWithMembers(teamId).orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        existingTeam.setHandle(updatedTeamVO.getHandle() != null ? updatedTeamVO.getHandle() : existingTeam.getHandle());
        existingTeam.setName(updatedTeamVO.getName() != null ? updatedTeamVO.getName() : existingTeam.getName());
        existingTeam.setDescription(updatedTeamVO.getDescription() != null ? updatedTeamVO.getDescription() : existingTeam.getDescription());

        Team updatedTeam = teamRepository.save(existingTeam);

        updatedTeam.setCreatedAt(updatedTeam.getCreatedAt().withZoneSameInstant(zoneId));
        updatedTeam.setUpdatedAt(updatedTeam.getUpdatedAt().withZoneSameInstant(zoneId));

        return convertToVO(updatedTeam);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long teamId) {
        if(!teamRepository.existsById(teamId)) throw new ResourceNotFoundException("Team not found with ID: " + teamId);

        teamRepository.deleteById(teamId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamVO addMember(TeamMemberId teamMemberId, ZoneId zoneId) {
        if(teamMemberId.getTeamId() == null || teamMemberId.getUserId() == null) throw new RequiredObjectIsNullException();

        Optional<Team> teamOpt = teamRepository.findByIdWithMembers(teamMemberId.getTeamId());
        Optional<User> userOpt = userRepository.findById(teamMemberId.getUserId());

        if (teamOpt.isEmpty()) {
            throw new ResourceNotFoundException("Team not found with the given ID: " + teamMemberId.getTeamId());
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with the given ID: " + teamMemberId.getUserId());
        }

        Team team = teamOpt.get();
        User user = userOpt.get();

        boolean isAlreadyMember = team.getTeamMembers().stream()
                .anyMatch(tm -> tm.getUser().equals(user));

        if(isAlreadyMember) throw new ResourceAlreadyExistsException("User is already a member of this team");

        TeamMember teamMember = new TeamMember();
        teamMember.setId(teamMemberId);
        teamMember.setTeam(team);
        teamMember.setUser(user);

        team.getTeamMembers().add(teamMember);
        teamRepository.save(team);

        team.setCreatedAt(team.getCreatedAt().withZoneSameInstant(zoneId));
        team.setUpdatedAt(team.getUpdatedAt().withZoneSameInstant(zoneId));

        return convertToVO(team);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMember(TeamMemberId teamMemberId) {
        Team team = teamRepository.findById(teamMemberId.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with the given ID: " + teamMemberId.getTeamId()));

        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember not found with the given IDs: teamId=" + teamMemberId.getTeamId() + ", userId=" + teamMemberId.getUserId()));

        team.getTeamMembers().remove(teamMember);

        teamMemberRepository.delete(teamMember);

        teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public List<TeamVO> findTeamsByUserId(Long userId, ZoneId zoneId) {
        if(userId == null) throw new RequiredObjectIsNullException();

        if(!userRepository.existsById(userId)) throw new ResourceNotFoundException("User not found with the given ID: " + userId);

        List<Team> teams = teamMemberRepository.findTeamsByUserId(userId);

        List<TeamVO> teamVOs = teams.stream().map(this::convertToVO).toList();

        teamVOs = teamVOs.stream().peek(teamVO -> {
            teamVO.setCreatedAt(teamVO.getCreatedAt().withZoneSameInstant(zoneId));
            teamVO.setUpdatedAt(teamVO.getUpdatedAt().withZoneSameInstant(zoneId));
        }).toList();

        return teamVOs;
    }

    private TeamVO convertToVO(Team team) {
        TeamVO teamVO = new TeamVO();
        teamVO.setKey(team.getId());
        teamVO.setHandle(team.getHandle());
        teamVO.setName(team.getName());
        teamVO.setDescription(team.getDescription());
        teamVO.setCreatedAt(team.getCreatedAt());
        teamVO.setUpdatedAt(team.getUpdatedAt());

        Set<Long> memberIds = team.getTeamMembers().stream()
                .map(tm -> tm.getUser().getId())
                .collect(Collectors.toSet());
        teamVO.setMembers(memberIds);

        teamVO.add(linkTo(methodOn(TeamController.class).findById(teamVO.getKey(), teamVO.getCreatedAt().getZone().toString())).withSelfRel());

        return teamVO;
    }
}