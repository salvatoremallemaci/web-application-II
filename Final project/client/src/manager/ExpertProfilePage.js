import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Row, Col, Button } from 'react-bootstrap';
import Loading from "../Loading";
import API from '../API';
import TicketsList from '../customer/TicketsList';
import ExpertLogSection from './ExpertLogSection';

function ExpertProfilePage() {
  let { expertId } = useParams();
  const [expert, setExpert] = useState({});
  const [dirty, setDirty] = useState(true);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (dirty) {
      API.getExpertById(expertId)
        .then(expert => {
          setExpert(expert);
          setDirty(false);
          setLoading(false);
        })
    }
  }, [expertId, dirty])

  useEffect(() => {
    if (!dirty && expert.id) {
      expert.ticketIds.forEach(ticketId => {
        if (!tickets.map(ticket => ticket.id).includes(ticketId)) {   // avoids duplications
          API.getTicketById(ticketId)
            .then(ticket => setTickets(prevTickets => {
              return prevTickets.concat(ticket);
            }))
            .catch(err => console.log(err))
        }
      });
    }
  }, [expertId, dirty])

  function authorizeExpert() {
    API.authorizeExpert(expert.id, !expert.authorized)
      .then(() => {
        setDirty(true);
      })
      .catch(err => console.log(err))
  }

  if (loading)
    return (<Loading />);

  return (
    <Row>
      <Col className='section'>
        <Row className='bottom-border'>
          <Col><h1 className='with-side-button'>{expert.firstName + ' ' + expert.lastName}</h1></Col>
          <Col className='d-flex justify-content-end'>
            <Button onClick={authorizeExpert}>{expert.authorized ? 'Unauthorize expert' : 'Authorize expert'}</Button>
          </Col>
        </Row>
        <Row>
          <Col xs={3} className='header-column'><h5 className='text-end'>Authorized</h5></Col>
          <Col>{expert.authorized ? 'Yes' : 'No'}</Col>
        </Row>
        <Row>
          <Col xs={3} className='header-column'><h5 className='text-end'>Expert's {expert.specializations?.length === 1 ? 'specialization' : 'specializations'}</h5></Col>
          <Col>{expert.specializations?.map(specialization => <p key={specialization.id}>{specialization.name}</p>)}</Col>
        </Row>

        <Row className='bottom-border'>
          <h2>Assigned tickets</h2>
        </Row>

        <Row>
          <TicketsList tickets={tickets} />
        </Row>

        <Row>
          <ExpertLogSection expertId={expertId} />
        </Row>
      </Col>
    </Row>
  );
}

export default ExpertProfilePage;