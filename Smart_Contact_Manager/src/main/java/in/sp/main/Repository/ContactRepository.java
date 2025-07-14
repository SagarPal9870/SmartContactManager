 package in.sp.main.Repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.sp.main.Entity.Contact;
import in.sp.main.Entity.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// ---Pagination-------
	@Query("from Contact as c where c.user.id= :userId")
	//current page  =page
	//contact per page=5
	public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pageable);
	//---------search---------------------
	public List<Contact> findByNameContainingAndUser(String name,User user);
}
