package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.persistence.model.User;

@Transactional(readOnly = true)
public interface UserRepository extends BaseEntityRepository<User> {

	Page<User> findByOwnerAndAccountStatusNot(String owner, AccountStatus status, Pageable pageable);

	Page<User> findByAccountStatusNot(AccountStatus status, Pageable pageable);

	Page<User> findByOwnerAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(String owner, String username, AccountStatus status,
			Pageable pageable);

	Page<User> findByUsernameStartingWithIgnoreCaseAndAccountStatusNot(String username, AccountStatus status, Pageable pageable);

	Page<User> findByOwnerAndEmailStartingWithIgnoreCaseAndAccountStatusNot(String owner, String email, AccountStatus status,
			Pageable pageable);

	Page<User> findByEmailStartingWithIgnoreCaseAndAccountStatusNot(String email, AccountStatus status, Pageable pageable);

	Page<User> findByOwnerAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(String owner,
			String username, String email, AccountStatus status, Pageable pageable);

	Page<User> findByUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(String username, String email,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndAccountStatusNot(String owner, AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(String owner, String username,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(String owner, String email,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
			String owner, String username, String email, AccountStatus status, Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus <> :status")
	Page<User> findByOwnerAndNetworkIdAndAccountStatusNot(@Param("owner") String owner, @Param("networkId") String networkId,
			@Param("status") AccountStatus status, Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus <> :status and lower(u.username) like lower(concat('%', :username, '%'))")
	Page<User> findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("username") String username, @Param("status") AccountStatus status,
			Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus <> :status and lower(u.email) like lower(concat('%', :email, '%'))")
	Page<User> findByOwnerAndNetworkIdAndEmailStartingWithIgnoreCaseAndAccountStatusNot(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("email") String email, @Param("status") AccountStatus status,
			Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus <> :status and lower(u.username) like lower(concat('%', :username, '%')) and lower(u.email) like lower(concat('%', :email, '%'))")
	Page<User> findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
			@Param("owner") String owner, @Param("networkId") String networkId, @Param("username") String username,
			@Param("email") String email, @Param("status") AccountStatus status, Pageable pageable);

	// Methods for filtering BY AccountStatus (equals) - for viewAllMode
	Page<User> findByAccountStatus(AccountStatus status, Pageable pageable);

	Page<User> findByUsernameStartingWithIgnoreCaseAndAccountStatus(String username, AccountStatus status, Pageable pageable);

	Page<User> findByEmailStartingWithIgnoreCaseAndAccountStatus(String email, AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndAccountStatus(String owner, AccountStatus status, Pageable pageable);

	Page<User> findByUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(String username, String email,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndAccountStatus(String owner, String username,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(String owner, String email,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
			String owner, String username, String email, AccountStatus status, Pageable pageable);

	// Methods for filtering BY AccountStatus (equals) - for owner-based mode
	Page<User> findByOwnerAndAccountStatus(String owner, AccountStatus status, Pageable pageable);

	Page<User> findByOwnerAndUsernameStartingWithIgnoreCaseAndAccountStatus(String owner, String username,
			AccountStatus status, Pageable pageable);

	Page<User> findByOwnerAndEmailStartingWithIgnoreCaseAndAccountStatus(String owner, String email, AccountStatus status,
			Pageable pageable);

	Page<User> findByOwnerAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(String owner,
			String username, String email, AccountStatus status, Pageable pageable);

	// Methods for filtering BY AccountStatus (equals) - for network-based mode
	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus = :status")
	Page<User> findByOwnerAndNetworkIdAndAccountStatus(@Param("owner") String owner, @Param("networkId") String networkId,
			@Param("status") AccountStatus status, Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus = :status and lower(u.username) like lower(concat('%', :username, '%'))")
	Page<User> findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndAccountStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("username") String username, @Param("status") AccountStatus status,
			Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus = :status and lower(u.email) like lower(concat('%', :email, '%'))")
	Page<User> findByOwnerAndNetworkIdAndEmailStartingWithIgnoreCaseAndAccountStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("email") String email, @Param("status") AccountStatus status,
			Pageable pageable);

	@Query("select distinct u from User u join u.groups g where g.network.id = :networkId and u.owner = :owner and u.accountStatus = :status and lower(u.username) like lower(concat('%', :username, '%')) and lower(u.email) like lower(concat('%', :email, '%'))")
	Page<User> findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
			@Param("owner") String owner, @Param("networkId") String networkId, @Param("username") String username,
			@Param("email") String email, @Param("status") AccountStatus status, Pageable pageable);
}
