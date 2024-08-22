package com.commerce.pal.backend.controller.user;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.dto.user.UserInvitationReq;
import com.commerce.pal.backend.dto.user.UserInvitationResponse;
import com.commerce.pal.backend.dto.user.UserInvitationSendReq;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.invitation.UserInvitation;
import com.commerce.pal.backend.module.users.UserInvitationService;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.user.invitation.UserInvitationRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/prime/api/v1/user-invitations"})
@RequiredArgsConstructor
public class UserInvitationController {

    private final UserInvitationService userInvitationService;
    private final GlobalMethods globalMethods;
    private final LoginValidationRepository loginValidationRepository;
    private final UserInvitationRepository userInvitationRepository;

    @PostMapping("/check")
    public ResponseEntity<List<UserInvitationResponse>> checkPhoneNumbers(@Valid @RequestBody UserInvitationReq request) {
        LoginValidation user = globalMethods.fetchUserDetails();
        List<UserInvitationResponse> response = userInvitationService.checkPhoneNumbers(user.getLoginId(), request.getPhoneNumbers());
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendInvitation(@Valid @RequestBody UserInvitationSendReq sendReq) {
        LoginValidation user = globalMethods.fetchUserDetails();

        String phoneNumber = sendReq.getPhoneNumber().replace(" ", "");
        String formattedPhoneNumber = GlobalMethods.formatPhoneNumberToEthiopianStandard(phoneNumber);

        if (loginValidationRepository.existsByPhoneNumber(formattedPhoneNumber)) {
            throw new ResourceNotFoundException("User with phone number " + phoneNumber + " already exists.");
        }

        Optional<UserInvitation> userInvitation = userInvitationRepository.findByPhoneNumber(formattedPhoneNumber);
        if (userInvitation.isPresent() && userInvitation.get().getInvitedBy().contains(user.getLoginId())) {
            throw new ResourceAlreadyExistsException("You have already sent invitation to " + phoneNumber + ".");
        }

        userInvitationService.sendInvitation(user, formattedPhoneNumber, sendReq.getAppLink());

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "success")
                .put("statusMessage", "successful");

        return ResponseEntity.ok(response.toString());
    }
}
